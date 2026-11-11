package ua.kpi.iot_lighting.prac2.domain.validation

/**
 * Перевірка профілю користувача та зміни пароля.
 * Поточний пароль передається окремо, щоб валідатор не залежав від Android API.
 */
object SettingsValidator {

    private val emailRegex =
        Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun validate(
        name: String,
        email: String,
        tariff: String,
        currentPassword: String,
        newPassword: String,
        confirmPassword: String,
        actualPassword: String
    ): SettingsFormValidation {
        val errors = mutableMapOf<SettingsField, String>()

        val trimmedName = name.trim()
        when {
            trimmedName.isEmpty() -> errors[SettingsField.NAME] = "Вкажіть ім'я"
            trimmedName.length < 2 -> errors[SettingsField.NAME] = "Мінімум 2 символи"
            trimmedName.length > 50 -> errors[SettingsField.NAME] = "Максимум 50 символів"
        }

        val trimmedEmail = email.trim()
        when {
            trimmedEmail.isEmpty() -> errors[SettingsField.EMAIL] = "Вкажіть email"
            !emailRegex.matches(trimmedEmail) -> errors[SettingsField.EMAIL] = "Некоректний формат email"
        }

        val tariffValue = tariff.toDoubleFlexible()
        when {
            tariff.isBlank() -> errors[SettingsField.TARIFF] = "Вкажіть тариф"
            tariffValue == null -> errors[SettingsField.TARIFF] = "Тариф має бути числом"
            tariffValue < 0.01 || tariffValue > 100.0 -> errors[SettingsField.TARIFF] = "Діапазон 0,01–100 ₴/кВт·год"
        }

        if (newPassword.isNotBlank()) {
            if (currentPassword.isBlank() || currentPassword != actualPassword) {
                errors[SettingsField.CURRENT_PASSWORD] = "Поточний пароль неправильний"
            }
            when {
                newPassword.length < 8 -> errors[SettingsField.NEW_PASSWORD] = "Мінімум 8 символів"
                !newPassword.any { it.isLetter() } || !newPassword.any { it.isDigit() } ->
                    errors[SettingsField.NEW_PASSWORD] = "Потрібні літери та цифри"
                newPassword == actualPassword -> errors[SettingsField.NEW_PASSWORD] = "Новий пароль має відрізнятися"
            }
            if (confirmPassword != newPassword) {
                errors[SettingsField.CONFIRM_PASSWORD] = "Паролі не співпадають"
            }
        }

        return SettingsFormValidation(errors.isEmpty(), errors)
    }
}
