package ua.kpi.iot_lighting.prac2.data.repository

import ua.kpi.iot_lighting.prac2.data.local.entity.DeviceEntity
import ua.kpi.iot_lighting.prac2.data.local.entity.EnergyReadingEntity
import ua.kpi.iot_lighting.prac2.data.local.entity.ZoneEntity
import ua.kpi.iot_lighting.prac2.data.local.relation.DailyEnergyTuple
import ua.kpi.iot_lighting.prac2.data.local.relation.DeviceEnergyProjection
import ua.kpi.iot_lighting.prac2.data.local.relation.ReadingRowTuple
import ua.kpi.iot_lighting.prac2.domain.model.DailyEnergy
import ua.kpi.iot_lighting.prac2.domain.model.DeviceEnergy
import ua.kpi.iot_lighting.prac2.domain.model.DeviceType
import ua.kpi.iot_lighting.prac2.domain.model.EnergyReading
import ua.kpi.iot_lighting.prac2.domain.model.LightingDevice
import ua.kpi.iot_lighting.prac2.domain.model.LightingZone
import ua.kpi.iot_lighting.prac2.domain.model.ReadingRow
import ua.kpi.iot_lighting.prac2.domain.model.ReadingSource

fun ZoneEntity.toDomain(): LightingZone = LightingZone(
    id = id,
    name = name,
    description = description
)

fun DeviceEntity.toDomain(): LightingDevice = LightingDevice(
    id = id,
    zoneId = zoneId,
    name = name,
    type = runCatching { DeviceType.valueOf(type) }.getOrDefault(DeviceType.LED),
    powerW = powerW,
    brightnessPercent = brightnessPercent,
    colorTempK = colorTempK,
    hoursPerDay = hoursPerDay,
    quantity = quantity,
    isPoweredOn = isPoweredOn,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun LightingDevice.toEntity(): DeviceEntity = DeviceEntity(
    id = id,
    zoneId = zoneId,
    name = name.trim(),
    type = type.name,
    powerW = powerW,
    brightnessPercent = brightnessPercent,
    colorTempK = colorTempK,
    hoursPerDay = hoursPerDay,
    quantity = quantity,
    isPoweredOn = isPoweredOn,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun EnergyReadingEntity.toDomain(): EnergyReading = EnergyReading(
    id = id,
    deviceId = deviceId,
    timestamp = timestamp,
    energyKwh = energyKwh,
    cost = cost,
    voltage = voltage,
    current = current,
    temperature = temperature,
    source = runCatching { ReadingSource.valueOf(source) }.getOrDefault(ReadingSource.LOCAL)
)

fun ReadingRowTuple.toDomain(): ReadingRow = ReadingRow(
    id = id,
    deviceId = deviceId,
    deviceName = deviceName,
    zoneName = zoneName,
    timestamp = timestamp,
    energyKwh = energyKwh,
    cost = cost,
    voltage = voltage,
    current = current,
    temperature = temperature,
    source = runCatching { ReadingSource.valueOf(source) }.getOrDefault(ReadingSource.LOCAL)
)

fun DailyEnergyTuple.toDomain(): DailyEnergy = DailyEnergy(
    day = day,
    totalKwh = totalKwh,
    totalCost = totalCost
)

fun DeviceEnergyProjection.toDomain(): DeviceEnergy = DeviceEnergy(
    id = id,
    name = name,
    zoneName = zoneName,
    dailyKwh = dailyKwh,
    monthlyKwh = monthlyKwh,
    monthlyCost = monthlyCost
)
