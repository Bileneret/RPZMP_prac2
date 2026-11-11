package ua.kpi.iot_lighting.prac2.data.repository

import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import ua.kpi.iot_lighting.prac2.data.local.AppDatabase
import ua.kpi.iot_lighting.prac2.data.local.dao.DeviceDao
import ua.kpi.iot_lighting.prac2.data.local.dao.EnergyReadingDao
import ua.kpi.iot_lighting.prac2.data.remote.MockEnergyRemoteDataSource
import ua.kpi.iot_lighting.prac2.data.settings.SettingsRepository
import ua.kpi.iot_lighting.prac2.domain.model.DailyEnergy
import ua.kpi.iot_lighting.prac2.domain.model.DeviceStats
import ua.kpi.iot_lighting.prac2.domain.model.EnergyPeriod
import ua.kpi.iot_lighting.prac2.domain.model.EnergySummary
import ua.kpi.iot_lighting.prac2.domain.model.HistoryFilter
import ua.kpi.iot_lighting.prac2.domain.model.ReadingRow
import ua.kpi.iot_lighting.prac2.domain.repository.EnergyRepository
import java.time.LocalDate
import java.time.ZoneId

class EnergyRepositoryImpl(
    private val database: AppDatabase,
    private val energyReadingDao: EnergyReadingDao,
    private val deviceDao: DeviceDao,
    private val settingsRepository: SettingsRepository,
    private val remoteDataSource: MockEnergyRemoteDataSource
) : EnergyRepository {

    private data class ReadingStats(
        val totalCost: Double,
        val averageReading: Double,
        val count: Int
    )

    private val deviceStats: Flow<DeviceStats> = combine(
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

    private val readingStats: Flow<ReadingStats> = combine(
        energyReadingDao.observeTotalCost(),
        energyReadingDao.observeAverageEnergy(),
        energyReadingDao.observeCount()
    ) { cost, average, count ->
        ReadingStats(
            totalCost = cost ?: 0.0,
            averageReading = average ?: 0.0,
            count = count
        )
    }

    override fun observeSummary(): Flow<EnergySummary> {
        val (monthStart, monthEnd) = currentMonthRange()
        val monthlyKwh = energyReadingDao.observeEnergyBetween(monthStart, monthEnd)
        return combine(
            deviceStats,
            readingStats,
            monthlyKwh,
            settingsRepository.settings
        ) { devices, readings, monthKwh, settings ->
            val actualMonthKwh = monthKwh ?: 0.0
            EnergySummary(
                totalDevices = devices.total,
                poweredOnDevices = devices.poweredOn,
                dailyKwh = devices.dailyKwh,
                monthlyKwh = actualMonthKwh,
                monthlyCost = actualMonthKwh * settings.tariff,
                averagePowerW = devices.averagePowerW,
                totalCost = readings.totalCost,
                averageReadingKwh = readings.averageReading,
                readingsCount = readings.count
            )
        }
    }

    override fun observeDailyTrend(
        period: EnergyPeriod,
        zoneId: Long?
    ): Flow<List<DailyEnergy>> {
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        val start = when (period) {
            EnergyPeriod.WEEK -> today.minusDays(6)
            EnergyPeriod.TWO_WEEKS -> today.minusDays(13)
            EnergyPeriod.MONTH -> today.withDayOfMonth(1)
        }
        val from = start.atStartOfDay(zone).toInstant().toEpochMilli()

        return energyReadingDao.observeDailyTotals(from, zoneId).map { rows ->
            val byDay = rows.associateBy { it.day }
            buildList {
                var date = start
                while (!date.isAfter(today)) {
                    val row = byDay[date.toString()]
                    add(row?.toDomain() ?: DailyEnergy(date.toString(), 0.0, 0.0))
                    date = date.plusDays(1)
                }
            }
        }
    }

    override fun observeReadings(filter: HistoryFilter): Flow<List<ReadingRow>> =
        energyReadingDao.observeHistory(
            zoneId = filter.zoneId,
            deviceId = filter.deviceId,
            sort = filter.sort.name
        ).map { list -> list.map { it.toDomain() } }

    override suspend fun refresh(): Result<Unit> = runCatching {
        val settings = settingsRepository.current()
        if (settings.simulateFailure) {
            error("Демонстраційна помилка оновлення. Вимкніть її в налаштуваннях.")
        }
        val devices = deviceDao.getAll()
        val remoteReadings = remoteDataSource.fetchReadings(devices, settings.tariff)
        val (dayStart, dayEnd) = currentDayRange()
        database.withTransaction {
            // Оновлення замінює показники поточного дня, а не додає дублікати.
            energyReadingDao.deleteRange(dayStart, dayEnd)
            energyReadingDao.insertAll(remoteReadings)
        }
    }

    override suspend fun deleteReading(id: Long) {
        energyReadingDao.deleteById(id)
    }

    override suspend fun clearHistory() {
        energyReadingDao.deleteAll()
    }

    /** Поточний календарний місяць: з 1-го числа до 1-го числа наступного. */
    private fun currentMonthRange(): Pair<Long, Long> {
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        val start = today.withDayOfMonth(1).atStartOfDay(zone).toInstant().toEpochMilli()
        val end = today.plusMonths(1).withDayOfMonth(1)
            .atStartOfDay(zone).toInstant().toEpochMilli()
        return start to end
    }

    /** Поточна доба — для заміни денних показників при оновленні. */
    private fun currentDayRange(): Pair<Long, Long> {
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        val start = today.atStartOfDay(zone).toInstant().toEpochMilli()
        val end = today.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
        return start to end
    }
}
