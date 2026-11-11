package ua.kpi.iot_lighting.prac2.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Таблиця світильників.
 * Зовнішній ключ на зону + унікальна назва + індекси для фільтрів.
 */
@Entity(
    tableName = "devices",
    foreignKeys = [
        ForeignKey(
            entity = ZoneEntity::class,
            parentColumns = ["id"],
            childColumns = ["zoneId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("zoneId"),
        Index(value = ["name"], unique = true)
    ]
)
data class DeviceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val zoneId: Long,
    val name: String,
    val type: String,
    val powerW: Double,
    val brightnessPercent: Int,
    val colorTempK: Int,
    val hoursPerDay: Double,
    val quantity: Int,
    val isPoweredOn: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)
