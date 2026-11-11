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
import ua.kpi.iot_lighting.prac2.domain.model.HistoryFilter
import ua.kpi.iot_lighting.prac2.domain.model.HistorySort
import ua.kpi.iot_lighting.prac2.domain.model.LightingDevice
import ua.kpi.iot_lighting.prac2.domain.model.LightingZone
import ua.kpi.iot_lighting.prac2.domain.model.ReadingRow
import ua.kpi.iot_lighting.prac2.domain.repository.DeviceRepository
import ua.kpi.iot_lighting.prac2.domain.repository.EnergyRepository

data class HistoryUiState(
    val readings: List<ReadingRow> = emptyList(),
    val zones: List<LightingZone> = emptyList(),
    val devices: List<LightingDevice> = emptyList(),
    val filter: HistoryFilter = HistoryFilter(),
    val isLoading: Boolean = true,
    val message: String? = null
)

/** Історія показників із Room, фільтрами та сортуванням. */
@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModel(
    private val energyRepository: EnergyRepository,
    deviceRepository: DeviceRepository
) : ViewModel() {

    private val filter = MutableStateFlow(HistoryFilter())
    private val message = MutableStateFlow<String?>(null)

    val uiState: StateFlow<HistoryUiState> = combine(
        filter.flatMapLatest { energyRepository.observeReadings(it) },
        deviceRepository.observeZones(),
        deviceRepository.observeDevices(DeviceFilter()),
        filter,
        message
    ) { readings, zones, devices, currentFilter, currentMessage ->
        HistoryUiState(
            readings = readings,
            zones = zones,
            devices = devices,
            filter = currentFilter,
            isLoading = false,
            message = currentMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HistoryUiState()
    )

    fun onZoneSelected(zoneId: Long?) {
        filter.update { it.copy(zoneId = zoneId, deviceId = null) }
    }

    fun onDeviceSelected(deviceId: Long?) {
        filter.update { it.copy(deviceId = deviceId) }
    }

    fun onSortChange(sort: HistorySort) {
        filter.update { it.copy(sort = sort) }
    }

    fun resetFilters() {
        filter.value = HistoryFilter()
    }

    fun deleteReading(id: Long) {
        viewModelScope.launch {
            runCatching { energyRepository.deleteReading(id) }
                .onSuccess { message.value = "Запис видалено" }
                .onFailure { error -> message.value = error.message ?: "Не вдалося видалити запис" }
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            runCatching { energyRepository.clearHistory() }
                .onSuccess { message.value = "Історію очищено" }
                .onFailure { error -> message.value = error.message ?: "Не вдалося очистити історію" }
        }
    }

    fun onMessageShown() {
        message.value = null
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val container = appContainer()
                HistoryViewModel(
                    energyRepository = container.energyRepository,
                    deviceRepository = container.deviceRepository
                )
            }
        }
    }
}
