package ua.kpi.iot_lighting.prac2.domain.repository

import kotlinx.coroutines.flow.Flow
import ua.kpi.iot_lighting.prac2.domain.model.DailyEnergy
import ua.kpi.iot_lighting.prac2.domain.model.EnergyPeriod
import ua.kpi.iot_lighting.prac2.domain.model.EnergySummary
import ua.kpi.iot_lighting.prac2.domain.model.HistoryFilter
import ua.kpi.iot_lighting.prac2.domain.model.ReadingRow

/** Контракт доступу до показників енергоспоживання. */
interface EnergyRepository {
    fun observeSummary(): Flow<EnergySummary>
    fun observeDailyTrend(period: EnergyPeriod, zoneId: Long?): Flow<List<DailyEnergy>>
    fun observeReadings(filter: HistoryFilter): Flow<List<ReadingRow>>
    suspend fun refresh(): Result<Unit>
    suspend fun deleteReading(id: Long)
    suspend fun clearHistory()
}
