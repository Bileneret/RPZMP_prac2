package ua.kpi.iot_lighting.prac2.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import ua.kpi.iot_lighting.prac2.data.local.dao.DeviceDao
import ua.kpi.iot_lighting.prac2.data.local.dao.EnergyReadingDao
import ua.kpi.iot_lighting.prac2.data.local.dao.ZoneDao
import ua.kpi.iot_lighting.prac2.data.local.entity.DeviceEntity
import ua.kpi.iot_lighting.prac2.data.local.entity.EnergyReadingEntity
import ua.kpi.iot_lighting.prac2.data.local.entity.ZoneEntity

@Database(
    entities = [ZoneEntity::class, DeviceEntity::class, EnergyReadingEntity::class],
    version = 2,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun zoneDao(): ZoneDao
    abstract fun deviceDao(): DeviceDao
    abstract fun energyReadingDao(): EnergyReadingDao

    companion object {
        /** v2: технічний isOnline замінено на користувацький isPoweredOn. */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE devices RENAME COLUMN isOnline TO isPoweredOn")
            }
        }
    }
}
