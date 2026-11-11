package ua.kpi.iot_lighting.prac2.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Таблиця історії енергоспоживання.
 * Каскадне видалення разом із пристроєм.
 */
@Entity(
    tableName = "energy_readings",
    foreignKeys = [
        ForeignKey(
            entity = DeviceEntity::class,
            parentColumns = ["id"],
            childColumns = ["deviceId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("deviceId"),
        Index("timestamp")
    ]
)
data class EnergyReadingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val deviceId: Long,
    val timestamp: Long,
    val energyKwh: Double,
    val cost: Double,
    val voltage: Double,
    val current: Double,
    val temperature: Double,
    val source: String
)
