package ua.kpi.iot_lighting.prac2.data.settings

import android.content.Context
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ua.kpi.iot_lighting.prac2.domain.model.UserSettings

/**
 * Збережені налаштування на SharedPreferences.
 * Назва/email/тариф редагуються у формі профілю.
 */
class SettingsRepository(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val _settings = MutableStateFlow(load())
    val settings: StateFlow<UserSettings> = _settings.asStateFlow()

    fun current(): UserSettings = _settings.value

    fun save(settings: UserSettings) {
        prefs.edit {
            putString(KEY_NAME, settings.name)
            putString(KEY_EMAIL, settings.email)
            putString(KEY_PASSWORD, settings.password)
            putString(KEY_TARIFF, settings.tariff.toString())
            putLong(KEY_DEFAULT_ZONE, settings.defaultZoneId ?: -1L)
            putBoolean(KEY_SIMULATE_FAILURE, settings.simulateFailure)
        }
        _settings.value = settings
    }

    fun verifyPassword(password: String): Boolean = _settings.value.password == password

    private fun load(): UserSettings {
        val default = UserSettings()
        return UserSettings(
            name = prefs.getString(KEY_NAME, default.name) ?: default.name,
            email = prefs.getString(KEY_EMAIL, default.email) ?: default.email,
            password = prefs.getString(KEY_PASSWORD, default.password) ?: default.password,
            tariff = prefs.getString(KEY_TARIFF, null)?.toDoubleOrNull() ?: default.tariff,
            defaultZoneId = prefs.getLong(KEY_DEFAULT_ZONE, -1L).takeIf { it > 0L },
            simulateFailure = prefs.getBoolean(KEY_SIMULATE_FAILURE, false)
        )
    }

    private companion object {
        const val PREFS_NAME = "iot_lighting_settings"
        const val KEY_NAME = "user_name"
        const val KEY_EMAIL = "user_email"
        const val KEY_PASSWORD = "user_password"
        const val KEY_TARIFF = "tariff"
        const val KEY_DEFAULT_ZONE = "default_zone"
        const val KEY_SIMULATE_FAILURE = "simulate_failure"
    }
}
