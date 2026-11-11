package ua.kpi.iot_lighting.prac2.di

import android.content.Context
import androidx.room.Room
import androidx.room.withTransaction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import ua.kpi.iot_lighting.prac2.data.local.AppDatabase
import ua.kpi.iot_lighting.prac2.data.local.SeedData
import ua.kpi.iot_lighting.prac2.data.remote.MockEnergyRemoteDataSource
import ua.kpi.iot_lighting.prac2.data.repository.DeviceRepositoryImpl
import ua.kpi.iot_lighting.prac2.data.repository.EnergyRepositoryImpl
import ua.kpi.iot_lighting.prac2.data.settings.SettingsRepository
import ua.kpi.iot_lighting.prac2.domain.repository.DeviceRepository
import ua.kpi.iot_lighting.prac2.domain.repository.EnergyRepository

/**
 * Ручний DI-контейнер: база, репозиторії та application-scope.
 * Для лабораторної цього достатньо; Hilt не ускладнює збірку.
 */
class AppContainer(context: Context) {

    private val appContext = context.applicationContext

    private val database: AppDatabase = Room.databaseBuilder(
        appContext,
        AppDatabase::class.java,
        "iot_lighting.db"
    )
        .addMigrations(AppDatabase.MIGRATION_1_2)
        .fallbackToDestructiveMigration(dropAllTables = true)
        .build()

    val settingsRepository = SettingsRepository(appContext)

    val deviceRepository: DeviceRepository = DeviceRepositoryImpl(
        database = database,
        zoneDao = database.zoneDao(),
        deviceDao = database.deviceDao(),
        energyReadingDao = database.energyReadingDao(),
        settingsRepository = settingsRepository
    )

    val energyRepository: EnergyRepository = EnergyRepositoryImpl(
        database = database,
        energyReadingDao = database.energyReadingDao(),
        deviceDao = database.deviceDao(),
        settingsRepository = settingsRepository,
        remoteDataSource = MockEnergyRemoteDataSource()
    )

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        applicationScope.launch { seedIfNeeded() }
    }

    private suspend fun seedIfNeeded() {
        if (database.zoneDao().count() > 0) return
        val now = System.currentTimeMillis()
        val devices = SeedData.devices(now)
        database.withTransaction {
            database.zoneDao().insertAll(SeedData.zones())
            database.deviceDao().insertAll(devices)
            database.energyReadingDao().insertAll(SeedData.readings(devices, now))
        }
    }
}
