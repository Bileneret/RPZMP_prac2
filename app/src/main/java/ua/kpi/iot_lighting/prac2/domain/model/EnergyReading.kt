package ua.kpi.iot_lighting.prac2.domain.model

/** Джерело походження показника енергоспоживання. */
enum class ReadingSource(val label: String) {
    LOCAL("Локальний"),
    REMOTE("Віддалений"),
    CALCULATED("Розрахунок")
}

/** Один запис історії енергоспоживання. */
data class EnergyReading(
    val id: Long = 0L,
    val deviceId: Long,
    val timestamp: Long,
    val energyKwh: Double,
    val cost: Double,
    val voltage: Double,
    val current: Double,
    val temperature: Double,
    val source: ReadingSource
)
