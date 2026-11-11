package ua.kpi.iot_lighting.prac2.domain.model

/** Агреговані показники для головного меню та дашборда. */
data class DeviceStats(
    val total: Int = 0,
    val poweredOn: Int = 0,
    val dailyKwh: Double = 0.0,
    val averagePowerW: Double = 0.0
)

/** Підсумок по енергоспоживанню для екрана аналітики. */
data class EnergySummary(
    val totalDevices: Int = 0,
    val poweredOnDevices: Int = 0,
    val dailyKwh: Double = 0.0,
    val monthlyKwh: Double = 0.0,
    val monthlyCost: Double = 0.0,
    val averagePowerW: Double = 0.0,
    val totalCost: Double = 0.0,
    val averageReadingKwh: Double = 0.0,
    val readingsCount: Int = 0
)

/** Точка графіка «енергія за день». */
data class DailyEnergy(
    val day: String,
    val totalKwh: Double,
    val totalCost: Double
)
