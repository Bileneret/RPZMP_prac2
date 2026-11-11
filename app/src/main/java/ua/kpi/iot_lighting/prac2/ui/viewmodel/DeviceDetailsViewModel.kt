package ua.kpi.iot_lighting.prac2.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ua.kpi.iot_lighting.prac2.domain.model.DeviceDetails
import ua.kpi.iot_lighting.prac2.domain.repository.DeviceRepository

data class DeviceDetailsUiState(
    val details: DeviceDetails? = null,
    val tariff: Double = 4.32,
    val isLoading: Boolean = true,
    val isDeleting: Boolean = false,
    val isDeleted: Boolean = false,
    val message: String? = null
)

private data class DeleteState(
    val isDeleting: Boolean = false,
    val isDeleted: Boolean = false,
    val message: String? = null
)

/** Деталі світильника. */
class DeviceDetailsViewModel(
    private val deviceRepository: DeviceRepository,
    settingsRepository: ua.kpi.iot_lighting.prac2.data.settings.SettingsRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val deviceId: Long = savedStateHandle.get<Long>("deviceId") ?: -1L
    private val deleteState = MutableStateFlow(DeleteState())

    val uiState: StateFlow<DeviceDetailsUiState> = combine(
        deviceRepository.observeDeviceDetails(deviceId),
        settingsRepository.settings,
        deleteState
    ) { details, settings, deletion ->
        DeviceDetailsUiState(
            details = details,
            tariff = settings.tariff,
            isLoading = false,
            isDeleting = deletion.isDeleting,
            isDeleted = deletion.isDeleted,
            message = deletion.message
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DeviceDetailsUiState()
    )

    fun delete() {
        if (deleteState.value.isDeleting) return
        viewModelScope.launch {
            deleteState.update { it.copy(isDeleting = true, message = null) }
            runCatching { deviceRepository.delete(deviceId) }
                .onSuccess {
                    deleteState.update {
                        it.copy(isDeleting = false, isDeleted = true, message = "Світильник видалено")
                    }
                }
                .onFailure { error ->
                    deleteState.update {
                        it.copy(
                            isDeleting = false,
                            message = error.message ?: "Не вдалося видалити світильник"
                        )
                    }
                }
        }
    }

    fun setPower(isOn: Boolean) {
        viewModelScope.launch {
            runCatching { deviceRepository.setPower(deviceId, isOn) }
                .onFailure { error ->
                    deleteState.update {
                        it.copy(message = error.message ?: "Не вдалося змінити стан світильника")
                    }
                }
        }
    }

    fun onMessageShown() {
        deleteState.update { it.copy(message = null) }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                DeviceDetailsViewModel(
                    deviceRepository = appContainer().deviceRepository,
                    settingsRepository = appContainer().settingsRepository,
                    savedStateHandle = createSavedStateHandle()
                )
            }
        }
    }
}
