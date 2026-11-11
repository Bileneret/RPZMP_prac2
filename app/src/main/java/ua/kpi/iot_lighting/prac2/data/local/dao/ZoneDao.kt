package ua.kpi.iot_lighting.prac2.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import ua.kpi.iot_lighting.prac2.data.local.entity.ZoneEntity

@Dao
interface ZoneDao {

    @Query("SELECT * FROM zones ORDER BY name ASC")
    fun observeAll(): Flow<List<ZoneEntity>>

    @Query("SELECT * FROM zones WHERE id = :id")
    fun observeById(id: Long): Flow<ZoneEntity?>

    @Query("SELECT * FROM zones ORDER BY name ASC")
    suspend fun getAll(): List<ZoneEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(zone: ZoneEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(zones: List<ZoneEntity>)

    @Update
    suspend fun update(zone: ZoneEntity)

    @Query("SELECT COUNT(*) FROM zones")
    suspend fun count(): Int
}
