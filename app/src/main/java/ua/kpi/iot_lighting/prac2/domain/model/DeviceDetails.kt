package ua.kpi.iot_lighting.prac2.domain.model

/** Деталі пристрою разом з останніми показниками та назвою зони. */
data class DeviceDetails(
    val device: LightingDevice,
    val readings: List<EnergyReading>,
    val zoneName: String
)
