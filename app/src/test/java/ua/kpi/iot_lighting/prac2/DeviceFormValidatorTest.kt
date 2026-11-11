package ua.kpi.iot_lighting.prac2

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import ua.kpi.iot_lighting.prac2.domain.validation.DeviceFormField
import ua.kpi.iot_lighting.prac2.domain.validation.DeviceFormValidator

class DeviceFormValidatorTest {

    private fun validate(
        name: String = "Люстра вітальня",
        zoneId: Long? = 1L,
        powerW: String = "24",
        brightness: String = "80",
        colorTempK: String = "4000",
        hoursPerDay: String = "6",
        quantity: String = "1"
    ) = DeviceFormValidator.validate(
        name = name,
        zoneId = zoneId,
        powerW = powerW,
        brightness = brightness,
        colorTempK = colorTempK,
        hoursPerDay = hoursPerDay,
        quantity = quantity
    )

    @Test
    fun validForm_hasNoErrors() {
        val result = validate()
        assertTrue(result.isValid)
        assertTrue(result.errors.isEmpty())
    }

    @Test
    fun blankName_isRejected() {
        val result = validate(name = " ")
        assertFalse(result.isValid)
        assertEquals("Вкажіть назву світильника", result.errors[DeviceFormField.NAME])
    }

    @Test
    fun shortName_isRejected() {
        val result = validate(name = "A")
        assertFalse(result.isValid)
        assertEquals("Мінімум 2 символи", result.errors[DeviceFormField.NAME])
    }

    @Test
    fun nullZone_isRejected() {
        val result = validate(zoneId = null)
        assertFalse(result.isValid)
        assertEquals("Оберіть зону", result.errors[DeviceFormField.ZONE])
    }

    @Test
    fun powerOutOfRange_isRejected() {
        val zero = validate(powerW = "0")
        assertFalse(zero.isValid)
        assertEquals("Діапазон 1–1000 Вт", zero.errors[DeviceFormField.POWER])

        val huge = validate(powerW = "1500")
        assertFalse(huge.isValid)
        assertEquals("Діапазон 1–1000 Вт", huge.errors[DeviceFormField.POWER])
    }

    @Test
    fun nonNumericPower_isRejected() {
        val result = validate(powerW = "abc")
        assertFalse(result.isValid)
        assertEquals("Потужність має бути числом", result.errors[DeviceFormField.POWER])
    }

    @Test
    fun commaDecimal_isAccepted() {
        val result = validate(powerW = "24,5", hoursPerDay = "6,5")
        assertTrue(result.isValid)
    }

    @Test
    fun brightnessOutOfRange_isRejected() {
        val result = validate(brightness = "120")
        assertFalse(result.isValid)
        assertEquals("Діапазон 0–100 %", result.errors[DeviceFormField.BRIGHTNESS])
    }

    @Test
    fun colorTemperatureOutOfRange_isRejected() {
        val result = validate(colorTempK = "7000")
        assertFalse(result.isValid)
        assertEquals("Діапазон 2000–6500 K", result.errors[DeviceFormField.COLOR_TEMP])
    }

    @Test
    fun hoursOutOfRange_isRejected() {
        val result = validate(hoursPerDay = "25")
        assertFalse(result.isValid)
        assertEquals("Діапазон 0–24 год", result.errors[DeviceFormField.HOURS])
    }

    @Test
    fun quantityOutOfRange_isRejected() {
        val result = validate(quantity = "0")
        assertFalse(result.isValid)
        assertEquals("Діапазон 1–1000 шт.", result.errors[DeviceFormField.QUANTITY])
    }

    @Test
    fun multipleErrors_areCollected() {
        val result = validate(name = "", zoneId = null, powerW = "", brightness = "101", colorTempK = "1000", hoursPerDay = "-1", quantity = "0")
        assertFalse(result.isValid)
        assertEquals(7, result.errors.size)
    }
}
