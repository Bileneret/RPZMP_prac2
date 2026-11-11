package ua.kpi.iot_lighting.prac2.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ua.kpi.iot_lighting.prac2.domain.model.DeviceFilter
import ua.kpi.iot_lighting.prac2.domain.model.DeviceSort
import ua.kpi.iot_lighting.prac2.domain.model.DeviceType
import ua.kpi.iot_lighting.prac2.domain.model.LightingDevice
import ua.kpi.iot_lighting.prac2.domain.model.LightingZone
import ua.kpi.iot_lighting.prac2.domain.repository.DeviceRepository

data class DeviceListUiState(
    val devices: List<LightingDevice> = emptyList(),
    val zones: List<LightingZone> = emptyList(),
    val filter: DeviceFilter = DeviceFilter(),
    val isLoading: Boolean = true,
    val message: String? = null
)

/** Список світильників із пошуком, фільтрами та сортуванням. */
@OptIn(ExperimentalCoroutinesApi::class)
class DeviceListViewModel(
    private val deviceRepository: DeviceRepository
) : ViewModel() {

    private val filter = MutableStateFlow(DeviceFilter())
    private val message = MutableStateFlow<String?>(null)

    val uiState: StateFlow<DeviceListUiState> = combine(
        filter.flatMapLatest { deviceRepository.observeDevices(it) },
        deviceRepository.observeZones(),
        filter,
        message
    ) { devices, zones, currentFilter, currentMessage ->
        DeviceListUiState(
            devices = devices,
            zones = zones,
            filter = currentFilter,
            isLoading = false,
            message = currentMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DeviceListUiState()
    )

    fun onQueryChange(query: String) {
        filter.update { it.copy(query = query) }
    }

    fun onZoneSelected(zoneId: Long?) {
        filter.update { it.copy(zoneId = zoneId) }
    }

    fun onTypeSelected(type: DeviceType?) {
        filter.update { it.copy(type = type) }
    }

    fun onOnlyPoweredOnChange(onlyPoweredOn: Boolean) {
        filter.update { it.copy(onlyPoweredOn = onlyPoweredOn) }
    }

    fun onSortChange(sort: DeviceSort) {
        filter.update { it.copy(sort = sort) }
    }

    fun resetFilters() {
        filter.value = DeviceFilter()
    }

    fun setPower(id: Long, isOn: Boolean) {
        viewModelScope.launch {
            runCatching { deviceRepository.setPower(id, isOn) }
                .onFailure { error ->
                    message.value = error.message ?: "Не вдалося змінити стан світильника"
                }
        }
    }

    fun setAllPower(isOn: Boolean) {
        viewModelScope.launch {
            runCatching { deviceRepository.setAllPower(isOn) }
                .onSuccess {
                    message.value = if (isOn) "Усі світильники увімкнено" else "Усі світильники вимкнено"
                }
                .onFailure { error ->
                    message.value = error.message ?: "Не вдалося змінити стан світильників"
                }
        }
    }

    fun deleteDevice(id: Long) {
        viewModelScope.launch {
            runCatching { deviceRepository.delete(id) }
                .onSuccess { message.value = "Світильник видалено" }
                .onFailure { error ->
                    message.value = error.message ?: "Не вдалося видалити світильник"
                }
        }
    }

    fun onMessageShown() {
        message.value = null
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                DeviceListViewModel(appContainer().deviceRepository)
            }
        }
    }
}
