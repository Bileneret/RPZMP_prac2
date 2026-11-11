package ua.kpi.iot_lighting.prac2.domain.model

/** Тип джерела світла. Використовується у формі та фільтрах. */
enum class DeviceType(val label: String) {
    LED("LED-світильник"),
    FLUORESCENT("Люмінесцентна лампа"),
    INCANDESCENT("Лампа розжарювання"),
    SMART("Smart-світильник")
}

/**
 * Доменна модель світильника.
 * Обчислювані властивості не зберігаються в БД, а розраховуються на льоту.
 */
data class LightingDevice(
    val id: Long = 0L,
    val zoneId: Long,
    val name: String,
    val type: DeviceType,
    val powerW: Double,
    val brightnessPercent: Int,
    val colorTempK: Int,
    val hoursPerDay: Double,
    val quantity: Int,
    val isPoweredOn: Boolean,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
) {
    val dailyKwh: Double
        get() = powerW * quantity * hoursPerDay / 1000.0

    val monthlyKwh: Double
        get() = dailyKwh * 30.0

    fun monthlyCost(tariff: Double): Double = monthlyKwh * tariff
}
