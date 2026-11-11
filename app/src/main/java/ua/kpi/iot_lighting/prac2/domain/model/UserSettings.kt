package ua.kpi.iot_lighting.prac2.domain.model

/** Збережені налаштування користувача. */
data class UserSettings(
    val name: String = "Недяк Олександр",
    val email: String = "student@kpi.ua",
    val password: String = "password123",
    val tariff: Double = 4.32,
    val defaultZoneId: Long? = null,
    val simulateFailure: Boolean = false
)

/** Агрегат по одному пристрою для рейтингу споживачів. */
data class DeviceEnergy(
    val id: Long,
    val name: String,
    val zoneName: String,
    val dailyKwh: Double,
    val monthlyKwh: Double,
    val monthlyCost: Double
)
