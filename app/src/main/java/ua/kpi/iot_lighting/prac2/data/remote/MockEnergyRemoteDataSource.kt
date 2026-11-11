package ua.kpi.iot_lighting.prac2.data.remote

import kotlinx.coroutines.delay
import ua.kpi.iot_lighting.prac2.data.local.entity.DeviceEntity
import ua.kpi.iot_lighting.prac2.data.local.entity.EnergyReadingEntity
import ua.kpi.iot_lighting.prac2.domain.model.ReadingSource

/**
 * Імітація віддаленого джерела даних.
 * Repository агрегує локальне сховище та «мережу», а результат кешується в Room.
 */
class MockEnergyRemoteDataSource {

    suspend fun fetchReadings(
        devices: List<DeviceEntity>,
        tariff: Double
    ): List<EnergyReadingEntity> {
        delay(900)
        val now = System.currentTimeMillis()
        return devices.mapIndexed { index, device ->
            val energy = device.powerW * device.quantity * device.hoursPerDay / 1000.0 *
                (0.94 + (index % 4) * 0.02)
            EnergyReadingEntity(
                deviceId = device.id,
                timestamp = now - index * 1_000L,
                energyKwh = energy,
                cost = energy * tariff,
                voltage = 230.0 + (index % 4),
                current = (device.powerW * device.quantity) / 230.0,
                temperature = 29.0 + (index % 6) * 0.5,
                source = ReadingSource.REMOTE.name
            )
        }
    }
}
