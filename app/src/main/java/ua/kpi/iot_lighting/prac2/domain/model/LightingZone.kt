package ua.kpi.iot_lighting.prac2.domain.model

/** Зона/приміщення, до якої прив'язані світильники. */
data class LightingZone(
    val id: Long = 0L,
    val name: String,
    val description: String
)
