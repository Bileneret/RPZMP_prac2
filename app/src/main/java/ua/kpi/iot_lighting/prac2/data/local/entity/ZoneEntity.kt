package ua.kpi.iot_lighting.prac2.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Таблиця зон/приміщень. */
@Entity(tableName = "zones")
data class ZoneEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val description: String
)
