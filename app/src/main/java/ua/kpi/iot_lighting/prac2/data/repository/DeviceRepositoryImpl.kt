package ua.kpi.iot_lighting.prac2.data.repository

import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import ua.kpi.iot_lighting.prac2.data.local.AppDatabase
import ua.kpi.iot_lighting.prac2.data.local.dao.DeviceDao
import ua.kpi.iot_lighting.prac2.data.local.dao.EnergyReadingDao
import ua.kpi.iot_lighting.prac2.data.local.dao.ZoneDao
import ua.kpi.iot_lighting.prac2.data.local.entity.EnergyReadingEntity
import ua.kpi.iot_lighting.prac2.data.settings.SettingsRepository
import ua.kpi.iot_lighting.prac2.domain.model.DeviceDetails
import ua.kpi.iot_lighting.prac2.domain.model.DeviceEnergy
import ua.kpi.iot_lighting.prac2.domain.model.DeviceFilter
import ua.kpi.iot_lighting.prac2.domain.model.DeviceStats
import ua.kpi.iot_lighting.prac2.domain.model.LightingDevice
import ua.kpi.iot_lighting.prac2.domain.model.LightingZone
import ua.kpi.iot_lighting.prac2.domain.model.ReadingSource
import ua.kpi.iot_lighting.prac2.domain.repository.DeviceRepository

class DeviceRepositoryImpl(
    private val database: AppDatabase,
    private val zoneDao: ZoneDao,
    private val deviceDao: DeviceDao,
    private val energyReadingDao: EnergyReadingDao,
    private val settingsRepository: SettingsRepository
) : DeviceRepository {

    override fun observeZones(): Flow<List<LightingZone>> =
        zoneDao.observeAll().map { zones -> zones.map { it.toDomain() } }

    override fun observeDevices(filter: DeviceFilter): Flow<List<LightingDevice>> =
        deviceDao.observeFiltered(
            query = filter.query.trim(),
            zoneId = filter.zoneId,
            type = filter.type?.name,
            onlyPoweredOn = filter.onlyPoweredOn,
            sort = filter.sort.name
        ).map { devices -> devices.map { it.toDomain() } }

    override fun observeDevice(id: Long): Flow<LightingDevice?> =
        deviceDao.observeById(id).map { it?.toDomain() }

    override fun observeDeviceDetails(id: Long): Flow<DeviceDetails?> =
        combine(
            deviceDao.observeWithReadings(id),
            zoneDao.observeAll()
        ) { relation, zones ->
            relation?.let {
                val zoneName = zones.firstOrNull { zone -> zone.id == it.device.zoneId }?.name
                    ?: "Без зони"
                DeviceDetails(
                    device = it.device.toDomain(),
                    readings = it.readings
                        .sortedByDescending { reading -> reading.timestamp }
                        .map { reading -> reading.toDomain() },
                    zoneName = zoneName
                )
            }
        }

    override fun observeStats(): Flow<DeviceStats> = combine(
        deviceDao.observeCount(),
        deviceDao.observePoweredOnCount(),
        deviceDao.observeDailyKwh(),
        deviceDao.observeAveragePower()
    ) { total, poweredOn, daily, averagePower ->
        val dailyKwh = daily ?: 0.0
        DeviceStats(
            total = total,
            poweredOn = poweredOn,
            dailyKwh = dailyKwh,
            averagePowerW = averagePower ?: 0.0
        )
    }

    override fun observeTopConsumers(tariff: Double): Flow<List<DeviceEnergy>> =
        deviceDao.observeDeviceEnergy(tariff).map { list -> list.map { it.toDomain() } }

    override suspend fun isNameTaken(name: String, excludeId: Long): Boolean =
        deviceDao.countByName(name.trim(), excludeId) > 0

    override suspend fun save(device: LightingDevice): Long = database.withTransaction {
        val now = System.currentTimeMillis()
        if (device.id == 0L) {
            val id = deviceDao.insert(device.copy(createdAt = now, updatedAt = now).toEntity())
            val tariff = settingsRepository.current().tariff
            energyReadingDao.insert(
                EnergyReadingEntity(
                    deviceId = id,
                    timestamp = now,
                    energyKwh = device.dailyKwh,
                    cost = device.dailyKwh * tariff,
                    voltage = 230.0,
                    current = (device.powerW * device.quantity) / 230.0,
                    temperature = 32.0,
                    source = ReadingSource.CALCULATED.name
                )
            )
            id
        } else {
            val existing = deviceDao.getById(device.id)
            deviceDao.update(
                device.copy(
                    createdAt = existing?.createdAt ?: device.createdAt,
                    updatedAt = now
                ).toEntity()
            )
            device.id
        }
    }

    override suspend fun delete(id: Long) {
        database.withTransaction {
            energyReadingDao.deleteByDevice(id)
            deviceDao.deleteById(id)
        }
    }

    override suspend fun setPower(id: Long, isOn: Boolean) {
        deviceDao.setPower(id = id, isOn = isOn, updatedAt = System.currentTimeMillis())
    }

    override suspend fun setAllPower(isOn: Boolean) {
        deviceDao.setAllPower(isOn = isOn, updatedAt = System.currentTimeMillis())
    }
}
