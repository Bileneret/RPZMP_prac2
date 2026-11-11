package ua.kpi.iot_lighting.prac2.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ua.kpi.iot_lighting.prac2.data.settings.SettingsRepository
import ua.kpi.iot_lighting.prac2.domain.validation.SettingsField
import ua.kpi.iot_lighting.prac2.domain.validation.SettingsValidator
import ua.kpi.iot_lighting.prac2.domain.validation.toDoubleFlexible

data class SettingsUiState(
    val name: String = "",
    val email: String = "",
    val tariff: String = "",
    val currentPassword: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val simulateFailure: Boolean = false,
    val isLoading: Boolean = true,
    val message: String? = null,
    val errors: Map<SettingsField, String> = emptyMap()
)

/** Форма профілю/налаштувань із валідацією. */
class SettingsViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val settings = settingsRepository.settings.first()
            _state.update {
                it.copy(
                    name = settings.name,
                    email = settings.email,
                    tariff = settings.tariff.formatInput(),
                    simulateFailure = settings.simulateFailure,
                    isLoading = false
                )
            }
        }
    }

    fun onNameChange(value: String) = updateField(SettingsField.NAME) { it.copy(name = value) }

    fun onEmailChange(value: String) = updateField(SettingsField.EMAIL) { it.copy(email = value) }

    fun onTariffChange(value: String) = updateField(SettingsField.TARIFF) { it.copy(tariff = value) }

    fun onCurrentPasswordChange(value: String) =
        updateField(SettingsField.CURRENT_PASSWORD) { it.copy(currentPassword = value) }

    fun onNewPasswordChange(value: String) =
        updateField(SettingsField.NEW_PASSWORD) { it.copy(newPassword = value) }

    fun onConfirmPasswordChange(value: String) =
        updateField(SettingsField.CONFIRM_PASSWORD) { it.copy(confirmPassword = value) }

    fun onSimulateFailureChange(value: Boolean) {
        _state.update { it.copy(simulateFailure = value) }
        settingsRepository.save(settingsRepository.current().copy(simulateFailure = value))
    }

    fun save() {
        val current = _state.value
        val stored = settingsRepository.current()
        val validation = SettingsValidator.validate(
            name = current.name,
            email = current.email,
            tariff = current.tariff,
            currentPassword = current.currentPassword,
            newPassword = current.newPassword,
            confirmPassword = current.confirmPassword,
            actualPassword = stored.password
        )
        if (!validation.isValid) {
            _state.update { it.copy(errors = validation.errors, message = null) }
            return
        }

        val newPassword = if (current.newPassword.isBlank()) stored.password else current.newPassword
        settingsRepository.save(
            stored.copy(
                name = current.name.trim(),
                email = current.email.trim(),
                password = newPassword,
                tariff = current.tariff.toDoubleFlexible() ?: stored.tariff,
                simulateFailure = current.simulateFailure
            )
        )

        _state.update {
            it.copy(
                errors = emptyMap(),
                message = "Налаштування збережено",
                currentPassword = "",
                newPassword = "",
                confirmPassword = ""
            )
        }
    }

    fun onMessageShown() {
        _state.update { it.copy(message = null) }
    }

    private fun updateField(
        field: SettingsField,
        transform: (SettingsUiState) -> SettingsUiState
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
                SettingsViewModel(appContainer().settingsRepository)
            }
        }
    }
}
