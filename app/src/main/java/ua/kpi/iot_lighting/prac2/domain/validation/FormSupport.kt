package ua.kpi.iot_lighting.prac2.domain.validation

/** Поля форми світильника, для яких показуються помилки. */
enum class DeviceFormField {
    NAME, ZONE, POWER, BRIGHTNESS, COLOR_TEMP, HOURS, QUANTITY
}

/** Поля форми налаштувань/профілю. */
enum class SettingsField {
    NAME, EMAIL, TARIFF, CURRENT_PASSWORD, NEW_PASSWORD, CONFIRM_PASSWORD
}

/** Результат перевірки форми світильника. */
data class DeviceFormValidation(
    val isValid: Boolean,
    val errors: Map<DeviceFormField, String> = emptyMap()
)

/** Результат перевірки форми налаштувань. */
data class SettingsFormValidation(
    val isValid: Boolean,
    val errors: Map<SettingsField, String> = emptyMap()
)

/** Парсить число з підтримкою десяткової коми та пробілів. */
fun String.toDoubleFlexible(): Double? = trim().replace(',', '.').toDoubleOrNull()

/** Парсить ціле число з підтримкою пробілів. */
fun String.toIntFlexible(): Int? = trim().toIntOrNull()
