package ua.kpi.iot_lighting.prac2.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ua.kpi.iot_lighting.prac2.domain.model.DeviceType
import ua.kpi.iot_lighting.prac2.domain.model.LightingDevice
import ua.kpi.iot_lighting.prac2.domain.model.LightingZone
import ua.kpi.iot_lighting.prac2.domain.repository.DeviceRepository
import ua.kpi.iot_lighting.prac2.domain.validation.DeviceFormField
import ua.kpi.iot_lighting.prac2.domain.validation.DeviceFormValidator
import ua.kpi.iot_lighting.prac2.domain.validation.toDoubleFlexible
import ua.kpi.iot_lighting.prac2.domain.validation.toIntFlexible

data class DeviceFormState(
    val id: Long? = null,
    val name: String = "",
    val zoneId: Long? = null,
    val type: DeviceType = DeviceType.LED,
    val powerW: String = "",
    val brightness: String = "80",
    val colorTempK: String = "4000",
    val hoursPerDay: String = "6",
    val quantity: String = "1",
    val isPoweredOn: Boolean = true,
    val zones: List<LightingZone> = emptyList(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val message: String? = null,
    val errors: Map<DeviceFormField, String> = emptyMap()
) {
    val isEditMode: Boolean get() = id != null && id > 0L
}

/**
 * Форма додавання/редагування світильника.
 * Вся валідація та асинхронне збереження — у ViewModel.
 */
class DeviceFormViewModel(
    private val deviceRepository: DeviceRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val deviceId: Long = savedStateHandle.get<Long>("deviceId") ?: -1L
    private val _state = MutableStateFlow(DeviceFormState())
    val uiState: StateFlow<DeviceFormState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val zones = deviceRepository.observeZones().first()
            val device = if (deviceId > 0L) deviceRepository.observeDevice(deviceId).first() else null
            _state.update { current ->
                if (device != null) {
                    current.copy(
                        id = device.id,
                        name = device.name,
                        zoneId = device.zoneId,
                        type = device.type,
                        powerW = device.powerW.formatInput(),
                        brightness = device.brightnessPercent.toString(),
                        colorTempK = device.colorTempK.toString(),
                        hoursPerDay = device.hoursPerDay.formatInput(),
                        quantity = device.quantity.toString(),
                        isPoweredOn = device.isPoweredOn,
                        zones = zones,
                        isLoading = false
                    )
                } else {
                    current.copy(
                        zones = zones,
                        zoneId = zones.firstOrNull()?.id,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun onNameChange(value: String) = updateField(DeviceFormField.NAME) { it.copy(name = value) }

    fun onZoneSelected(zoneId: Long) = updateField(DeviceFormField.ZONE) { it.copy(zoneId = zoneId) }

    fun onTypeSelected(type: DeviceType) = _state.update { it.copy(type = type, message = null) }

    fun onPowerChange(value: String) = updateField(DeviceFormField.POWER) { it.copy(powerW = value) }

    fun onBrightnessChange(value: String) =
        updateField(DeviceFormField.BRIGHTNESS) { it.copy(brightness = value) }

    fun onColorTempChange(value: String) =
        updateField(DeviceFormField.COLOR_TEMP) { it.copy(colorTempK = value) }

    fun onHoursChange(value: String) =
        updateField(DeviceFormField.HOURS) { it.copy(hoursPerDay = value) }

    fun onQuantityChange(value: String) =
        updateField(DeviceFormField.QUANTITY) { it.copy(quantity = value) }

    fun onPowerChange(value: Boolean) = _state.update { it.copy(isPoweredOn = value) }

    fun save() {
        val current = _state.value
        if (current.isSaving) return

        val validation = DeviceFormValidator.validate(
            name = current.name,
            zoneId = current.zoneId,
            powerW = current.powerW,
            brightness = current.brightness,
            colorTempK = current.colorTempK,
            hoursPerDay = current.hoursPerDay,
            quantity = current.quantity
        )
        if (!validation.isValid) {
            _state.update { it.copy(errors = validation.errors) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, errors = emptyMap(), message = null) }
            if (deviceRepository.isNameTaken(current.name.trim(), current.id ?: -1L)) {
                _state.update {
                    it.copy(
                        isSaving = false,
                        errors = it.errors + (DeviceFormField.NAME to "Світильник з такою назвою вже існує")
                    )
                }
                return@launch
            }

            val device = LightingDevice(
                id = current.id ?: 0L,
                zoneId = current.zoneId ?: 0L,
                name = current.name.trim(),
                type = current.type,
                powerW = current.powerW.toDoubleFlexible() ?: 0.0,
                brightnessPercent = current.brightness.toIntFlexible() ?: 0,
                colorTempK = current.colorTempK.toIntFlexible() ?: 4000,
                hoursPerDay = current.hoursPerDay.toDoubleFlexible() ?: 0.0,
                quantity = current.quantity.toIntFlexible() ?: 1,
                isPoweredOn = current.isPoweredOn
            )

            runCatching { deviceRepository.save(device) }
                .onSuccess {
                    _state.update {
                        it.copy(
                            isSaving = false,
                            isSaved = true,
                            message = "Світильник збережено"
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isSaving = false,
                            message = error.message ?: "Не вдалося зберегти світильник"
                        )
                    }
                }
        }
    }

    fun onMessageShown() {
        _state.update { it.copy(message = null) }
    }

    private fun updateField(
        field: DeviceFormField,
        transform: (DeviceFormState) -> DeviceFormState
    ) {
        _state.update { current ->
            transform(current).copy(errors = current.errors - field, message = null)
        }
    }

    private fun Double.formatInput(): String =
        if (this % 1.0 == 0.0) toInt().toString() else toString().replace('.', ',')

    companion object {
        val Factory = viewModelFactory {
            initializer {
                DeviceFormViewModel(
                    deviceRepository = appContainer().deviceRepository,
                    savedStateHandle = createSavedStateHandle()
                )
            }
        }
    }
}
