package ua.kpi.iot_lighting.prac2.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ua.kpi.iot_lighting.prac2.data.local.entity.EnergyReadingEntity
import ua.kpi.iot_lighting.prac2.data.local.relation.DailyEnergyTuple
import ua.kpi.iot_lighting.prac2.data.local.relation.ReadingRowTuple

@Dao
interface EnergyReadingDao {

    @Query("SELECT * FROM energy_readings ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<EnergyReadingEntity>>

    @Query("SELECT * FROM energy_readings WHERE deviceId = :deviceId ORDER BY timestamp DESC")
    fun observeByDevice(deviceId: Long): Flow<List<EnergyReadingEntity>>

    /**
     * Історія з JOIN на пристрій та зону.
     * Сортування перемикається через CASE, фільтри — через nullable-параметри.
     */
    @Query(
        """
        SELECT r.id,
               r.deviceId,
               d.name AS deviceName,
               z.name AS zoneName,
               r.timestamp,
               r.energyKwh,
               r.cost,
               r.voltage,
               r.current,
               r.temperature,
               r.source
        FROM energy_readings r
        INNER JOIN devices d ON d.id = r.deviceId
        INNER JOIN zones z ON z.id = d.zoneId
        WHERE (:zoneId IS NULL OR d.zoneId = :zoneId)
          AND (:deviceId IS NULL OR r.deviceId = :deviceId)
        ORDER BY
          CASE WHEN :sort = 'ENERGY_DESC' THEN r.energyKwh END DESC,
          CASE WHEN :sort = 'COST_DESC' THEN r.cost END DESC,
          r.timestamp DESC
        """
    )
    fun observeHistory(
        zoneId: Long?,
        deviceId: Long?,
        sort: String
    ): Flow<List<ReadingRowTuple>>

    /** Денний агрегат для графіка; STRFTIME + GROUP BY виконуються в SQLite. */
    @Query(
        """
        SELECT strftime('%Y-%m-%d', r.timestamp / 1000, 'unixepoch', 'localtime') AS day,
               SUM(r.energyKwh) AS totalKwh,
               SUM(r.cost) AS totalCost
        FROM energy_readings r
        INNER JOIN devices d ON d.id = r.deviceId
        WHERE r.timestamp >= :from
          AND (:zoneId IS NULL OR d.zoneId = :zoneId)
        GROUP BY day
        ORDER BY day ASC
        """
    )
    fun observeDailyTotals(from: Long, zoneId: Long?): Flow<List<DailyEnergyTuple>>

    @Query("SELECT SUM(energyKwh) FROM energy_readings")
    fun observeTotalEnergy(): Flow<Double?>

    @Query("SELECT SUM(cost) FROM energy_readings")
    fun observeTotalCost(): Flow<Double?>

    @Query("SELECT SUM(energyKwh) FROM energy_readings WHERE timestamp >= :from AND timestamp < :to")
    fun observeEnergyBetween(from: Long, to: Long): Flow<Double?>

    @Query("DELETE FROM energy_readings WHERE timestamp >= :from AND timestamp < :to")
    suspend fun deleteRange(from: Long, to: Long)

    @Query("SELECT AVG(energyKwh) FROM energy_readings")
    fun observeAverageEnergy(): Flow<Double?>

    @Query("SELECT COUNT(*) FROM energy_readings")
    fun observeCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(reading: EnergyReadingEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAll(readings: List<EnergyReadingEntity>)

    @Query("DELETE FROM energy_readings WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM energy_readings WHERE deviceId = :deviceId")
    suspend fun deleteByDevice(deviceId: Long)

    @Query("DELETE FROM energy_readings")
    suspend fun deleteAll()
}
