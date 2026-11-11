package ua.kpi.iot_lighting.prac2.domain.repository

import kotlinx.coroutines.flow.Flow
import ua.kpi.iot_lighting.prac2.domain.model.DeviceDetails
import ua.kpi.iot_lighting.prac2.domain.model.DeviceEnergy
import ua.kpi.iot_lighting.prac2.domain.model.DeviceFilter
import ua.kpi.iot_lighting.prac2.domain.model.DeviceStats
import ua.kpi.iot_lighting.prac2.domain.model.LightingDevice
import ua.kpi.iot_lighting.prac2.domain.model.LightingZone

/** Контракт доступу до світильників і зон. */
interface DeviceRepository {
    fun observeZones(): Flow<List<LightingZone>>
    fun observeDevices(filter: DeviceFilter): Flow<List<LightingDevice>>
    fun observeDevice(id: Long): Flow<LightingDevice?>
    fun observeDeviceDetails(id: Long): Flow<DeviceDetails?>
    fun observeStats(): Flow<DeviceStats>
    fun observeTopConsumers(tariff: Double): Flow<List<DeviceEnergy>>
    suspend fun isNameTaken(name: String, excludeId: Long = -1L): Boolean
    suspend fun save(device: LightingDevice): Long
    suspend fun delete(id: Long)
    suspend fun setPower(id: Long, isOn: Boolean)
    suspend fun setAllPower(isOn: Boolean)
}
