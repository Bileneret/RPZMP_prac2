package ua.kpi.iot_lighting.prac2.data.local

import ua.kpi.iot_lighting.prac2.data.local.entity.DeviceEntity
import ua.kpi.iot_lighting.prac2.data.local.entity.EnergyReadingEntity
import ua.kpi.iot_lighting.prac2.data.local.entity.ZoneEntity
import ua.kpi.iot_lighting.prac2.domain.model.DeviceType
import ua.kpi.iot_lighting.prac2.domain.model.ReadingSource

/** Детерміновані початкові дані для варіанта 13. */
object SeedData {

    const val DEFAULT_TARIFF = 4.32

    fun zones(): List<ZoneEntity> = listOf(
        ZoneEntity(1, "Вітальня", "Основне освітлення та зона відпочинку"),
        ZoneEntity(2, "Кухня", "Робоча зона та підсвітка"),
        ZoneEntity(3, "Спальня", "Вечірнє освітлення"),
        ZoneEntity(4, "Кабінет", "Робоче місце та читання")
    )

    fun devices(now: Long): List<DeviceEntity> = listOf(
        DeviceEntity(1, 1, "Люстра вітальня", DeviceType.LED.name, 24.0, 80, 4000, 6.0, 1, true, now, now),
        DeviceEntity(2, 1, "Торшер читання", DeviceType.LED.name, 12.0, 70, 3500, 4.0, 1, true, now, now),
        DeviceEntity(3, 2, "Підсвітка кухні", DeviceType.LED.name, 18.0, 100, 5000, 5.0, 2, true, now, now),
        DeviceEntity(4, 3, "Бра спальня", DeviceType.LED.name, 8.0, 30, 2700, 3.0, 2, false, now, now),
        DeviceEntity(5, 4, "Настільна лампа", DeviceType.LED.name, 10.0, 90, 4500, 8.0, 1, true, now, now),
        DeviceEntity(6, 1, "Галогенна лампа", DeviceType.INCANDESCENT.name, 50.0, 60, 3000, 2.0, 2, false, now, now),
        DeviceEntity(7, 2, "Люмінесцентна кухня", DeviceType.FLUORESCENT.name, 36.0, 100, 4000, 6.0, 1, true, now, now),
        DeviceEntity(8, 4, "Smart-стрічка", DeviceType.SMART.name, 14.0, 55, 6000, 7.0, 3, true, now, now)
    )

    fun readings(devices: List<DeviceEntity>, now: Long): List<EnergyReadingEntity> {
        val result = mutableListOf<EnergyReadingEntity>()
        devices.forEachIndexed { index, device ->
            for (day in 0..30) {
                val energy = device.powerW * device.quantity * device.hoursPerDay / 1000.0 *
                    (0.86 + 0.03 * (day % 4))
                result += EnergyReadingEntity(
                    deviceId = device.id,
                    timestamp = now - day * 86_400_000L - index * 60_000L,
                    energyKwh = energy,
                    cost = energy * DEFAULT_TARIFF,
                    voltage = 230.0 + (day % 5),
                    current = (device.powerW * device.quantity) / 230.0,
                    temperature = 28.0 + (day % 7) + index * 0.3,
                    source = ReadingSource.CALCULATED.name
                )
            }
        }
        return result
    }
}
