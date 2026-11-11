package ua.kpi.iot_lighting.prac2.domain.validation

/**
 * Комплексна перевірка форми світильника.
 * Винесена з UI, щоб її можна було покрити unit-тестами.
 */
object DeviceFormValidator {

    fun validate(
        name: String,
        zoneId: Long?,
        powerW: String,
        brightness: String,
        colorTempK: String,
        hoursPerDay: String,
        quantity: String
    ): DeviceFormValidation {
        val errors = mutableMapOf<DeviceFormField, String>()

        val trimmedName = name.trim()
        when {
            trimmedName.isEmpty() -> errors[DeviceFormField.NAME] = "Вкажіть назву світильника"
            trimmedName.length < 2 -> errors[DeviceFormField.NAME] = "Мінімум 2 символи"
            trimmedName.length > 40 -> errors[DeviceFormField.NAME] = "Максимум 40 символів"
        }

        if (zoneId == null || zoneId <= 0L) {
            errors[DeviceFormField.ZONE] = "Оберіть зону"
        }

        val power = powerW.toDoubleFlexible()
        when {
            powerW.isBlank() -> errors[DeviceFormField.POWER] = "Вкажіть потужність"
            power == null -> errors[DeviceFormField.POWER] = "Потужність має бути числом"
            power < 1.0 || power > 1000.0 -> errors[DeviceFormField.POWER] = "Діапазон 1–1000 Вт"
        }

        val brightnessValue = brightness.toIntFlexible()
        when {
            brightness.isBlank() -> errors[DeviceFormField.BRIGHTNESS] = "Вкажіть яскравість"
            brightnessValue == null -> errors[DeviceFormField.BRIGHTNESS] = "Яскравість має бути цілим числом"
            brightnessValue !in 0..100 -> errors[DeviceFormField.BRIGHTNESS] = "Діапазон 0–100 %"
        }

        val colorTempValue = colorTempK.toIntFlexible()
        when {
            colorTempK.isBlank() -> errors[DeviceFormField.COLOR_TEMP] = "Вкажіть температуру"
            colorTempValue == null -> errors[DeviceFormField.COLOR_TEMP] = "Температура має бути цілим числом"
            colorTempValue !in 2000..6500 -> errors[DeviceFormField.COLOR_TEMP] = "Діапазон 2000–6500 K"
        }

        val hoursValue = hoursPerDay.toDoubleFlexible()
        when {
            hoursPerDay.isBlank() -> errors[DeviceFormField.HOURS] = "Вкажіть години роботи"
            hoursValue == null -> errors[DeviceFormField.HOURS] = "Години мають бути числом"
            hoursValue < 0.0 || hoursValue > 24.0 -> errors[DeviceFormField.HOURS] = "Діапазон 0–24 год"
        }

        val quantityValue = quantity.toIntFlexible()
        when {
            quantity.isBlank() -> errors[DeviceFormField.QUANTITY] = "Вкажіть кількість"
            quantityValue == null -> errors[DeviceFormField.QUANTITY] = "Кількість має бути цілим числом"
            quantityValue !in 1..1000 -> errors[DeviceFormField.QUANTITY] = "Діапазон 1–1000 шт."
        }

        return DeviceFormValidation(errors.isEmpty(), errors)
    }
}
