package ua.kpi.iot_lighting.prac2.domain.model

/**
 * Рядок історії з даними пристрою та зони.
 * Формується JOIN-запитом Room, тому містить імена для UI.
 */
data class ReadingRow(
    val id: Long,
    val deviceId: Long,
    val deviceName: String,
    val zoneName: String,
    val timestamp: Long,
    val energyKwh: Double,
    val cost: Double,
    val voltage: Double,
    val current: Double,
    val temperature: Double,
    val source: ReadingSource
)
