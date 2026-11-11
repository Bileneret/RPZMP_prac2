package ua.kpi.iot_lighting.prac2.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import ua.kpi.iot_lighting.prac2.data.local.entity.DeviceEntity
import ua.kpi.iot_lighting.prac2.data.local.entity.EnergyReadingEntity

/** Пристрій разом з усіма його показниками (1:N). */
data class DeviceWithReadings(
    @Embedded val device: DeviceEntity,
    @Relation(parentColumn = "id", entityColumn = "deviceId")
    val readings: List<EnergyReadingEntity>
)

/** Плоский рядок JOIN-запиту для екрана історії. */
data class ReadingRowTuple(
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
    val source: String
)

/** Агрегат по днях для графіка. */
data class DailyEnergyTuple(
    val day: String,
    val totalKwh: Double,
    val totalCost: Double
)

/** Обчислювані поля по пристрою (SQL-проєкція). */
data class DeviceEnergyProjection(
    val id: Long,
    val name: String,
    val zoneName: String,
    val dailyKwh: Double,
    val monthlyKwh: Double,
    val monthlyCost: Double
)
