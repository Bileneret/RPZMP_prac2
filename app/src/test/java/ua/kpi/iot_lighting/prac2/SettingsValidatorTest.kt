package ua.kpi.iot_lighting.prac2

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import ua.kpi.iot_lighting.prac2.domain.validation.SettingsField
import ua.kpi.iot_lighting.prac2.domain.validation.SettingsValidator

class SettingsValidatorTest {

    private val actualPassword = "password123"

    private fun validate(
        name: String = "Недяк Олександр",
        email: String = "student@kpi.ua",
        tariff: String = "4,32",
        currentPassword: String = "",
        newPassword: String = "",
        confirmPassword: String = ""
    ) = SettingsValidator.validate(
        name = name,
        email = email,
        tariff = tariff,
        currentPassword = currentPassword,
        newPassword = newPassword,
        confirmPassword = confirmPassword,
        actualPassword = actualPassword
    )

    @Test
    fun validProfile_withoutPasswordChange_isAccepted() {
        val result = validate()
        assertTrue(result.isValid)
        assertTrue(result.errors.isEmpty())
    }

    @Test
    fun invalidEmail_isRejected() {
        val result = validate(email = "student.kpi.ua")
        assertFalse(result.isValid)
        assertEquals("Некоректний формат email", result.errors[SettingsField.EMAIL])
    }

    @Test
    fun invalidTariff_isRejected() {
        val result = validate(tariff = "0")
        assertFalse(result.isValid)
        assertEquals("Діапазон 0,01–100 ₴/кВт·год", result.errors[SettingsField.TARIFF])
    }

    @Test
    fun weakNewPassword_isRejected() {
        val result = validate(
            currentPassword = actualPassword,
            newPassword = "12345678",
            confirmPassword = "12345678"
        )
        assertFalse(result.isValid)
        assertEquals("Потрібні літери та цифри", result.errors[SettingsField.NEW_PASSWORD])
    }

    @Test
    fun wrongCurrentPassword_isRejected() {
        val result = validate(
            currentPassword = "wrong-password",
            newPassword = "newpass123",
            confirmPassword = "newpass123"
        )
        assertFalse(result.isValid)
        assertEquals("Поточний пароль неправильний", result.errors[SettingsField.CURRENT_PASSWORD])
    }

    @Test
    fun mismatchedConfirmation_isRejected() {
        val result = validate(
            currentPassword = actualPassword,
            newPassword = "newpass123",
            confirmPassword = "newpass124"
        )
        assertFalse(result.isValid)
        assertEquals("Паролі не співпадають", result.errors[SettingsField.CONFIRM_PASSWORD])
    }

    @Test
    fun samePassword_isRejected() {
        val result = validate(
            currentPassword = actualPassword,
            newPassword = actualPassword,
            confirmPassword = actualPassword
        )
        assertFalse(result.isValid)
        assertEquals("Новий пароль має відрізнятися", result.errors[SettingsField.NEW_PASSWORD])
    }

    @Test
    fun strongNewPassword_isAccepted() {
        val result = validate(
            currentPassword = actualPassword,
            newPassword = "newpass123",
            confirmPassword = "newpass123"
        )
        assertTrue(result.isValid)
    }
}
