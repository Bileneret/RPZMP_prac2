package ua.kpi.iot_lighting.prac2.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import ua.kpi.iot_lighting.prac2.data.local.entity.DeviceEntity
import ua.kpi.iot_lighting.prac2.data.local.relation.DeviceEnergyProjection
import ua.kpi.iot_lighting.prac2.data.local.relation.DeviceWithReadings

@Dao
interface DeviceDao {

    /**
     * Один SQL-запит покриває пошук, фільтри та сортування.
     * CASE у ORDER BY дозволяє перемикати порядок без дублювання методів.
     */
    @Query(
        """
        SELECT * FROM devices
        WHERE (:query = '' OR name LIKE '%' || :query || '%')
          AND (:zoneId IS NULL OR zoneId = :zoneId)
          AND (:type IS NULL OR type = :type)
          AND (:onlyPoweredOn = 0 OR isPoweredOn = 1)
        ORDER BY
          CASE WHEN :sort = 'POWER_DESC' THEN powerW END DESC,
          CASE WHEN :sort = 'NEWEST' THEN createdAt END DESC,
          name ASC
        """
    )
    fun observeFiltered(
        query: String,
        zoneId: Long?,
        type: String?,
        onlyPoweredOn: Boolean,
        sort: String
    ): Flow<List<DeviceEntity>>

    @Query("SELECT * FROM devices ORDER BY name ASC")
    fun observeAll(): Flow<List<DeviceEntity>>

    @Query("SELECT * FROM devices ORDER BY name ASC")
    suspend fun getAll(): List<DeviceEntity>

    @Query("SELECT * FROM devices WHERE id = :id")
    fun observeById(id: Long): Flow<DeviceEntity?>

    @Query("SELECT * FROM devices WHERE id = :id")
    suspend fun getById(id: Long): DeviceEntity?

    @Query("SELECT COUNT(*) FROM devices WHERE name = :name AND id != :excludeId")
    suspend fun countByName(name: String, excludeId: Long): Int

    @Query("SELECT COUNT(*) FROM devices")
    fun observeCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM devices WHERE isPoweredOn = 1")
    fun observePoweredOnCount(): Flow<Int>

    @Query("SELECT SUM(powerW * quantity * hoursPerDay / 1000.0) FROM devices")
    fun observeDailyKwh(): Flow<Double?>

    @Query("SELECT AVG(powerW) FROM devices")
    fun observeAveragePower(): Flow<Double?>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(device: DeviceEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(devices: List<DeviceEntity>)

    @Update
    suspend fun update(device: DeviceEntity)

    @Query("DELETE FROM devices WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE devices SET isPoweredOn = :isOn, updatedAt = :updatedAt WHERE id = :id")
    suspend fun setPower(id: Long, isOn: Boolean, updatedAt: Long)

    @Query("UPDATE devices SET isPoweredOn = :isOn, updatedAt = :updatedAt")
    suspend fun setAllPower(isOn: Boolean, updatedAt: Long)

    @Transaction
    @Query("SELECT * FROM devices WHERE id = :id")
    fun observeWithReadings(id: Long): Flow<DeviceWithReadings?>

    /** Обчислювані поля dailyKwh/monthlyKwh/monthlyCost прямо в SQL. */
    @Query(
        """
        SELECT d.id,
               d.name,
               z.name AS zoneName,
               d.powerW * d.quantity * d.hoursPerDay / 1000.0 AS dailyKwh,
               d.powerW * d.quantity * d.hoursPerDay * 30.0 / 1000.0 AS monthlyKwh,
               d.powerW * d.quantity * d.hoursPerDay * 30.0 / 1000.0 * :tariff AS monthlyCost
        FROM devices d
        INNER JOIN zones z ON z.id = d.zoneId
        ORDER BY dailyKwh DESC
        """
    )
    fun observeDeviceEnergy(tariff: Double): Flow<List<DeviceEnergyProjection>>
}
