package ua.kpi.iot_lighting.prac2.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ua.kpi.iot_lighting.prac2.domain.model.DailyEnergy
import ua.kpi.iot_lighting.prac2.domain.model.DeviceEnergy
import ua.kpi.iot_lighting.prac2.domain.model.EnergyPeriod
import ua.kpi.iot_lighting.prac2.domain.model.EnergySummary
import ua.kpi.iot_lighting.prac2.domain.model.LightingZone
import ua.kpi.iot_lighting.prac2.domain.repository.DeviceRepository
import ua.kpi.iot_lighting.prac2.domain.repository.EnergyRepository

private data class RefreshState(
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val lastUpdated: Long? = null
)

private data class EnergyFilters(
    val zoneId: Long?,
    val period: EnergyPeriod
)

data class EnergyUiState(
    val summary: EnergySummary = EnergySummary(),
    val trend: List<DailyEnergy> = emptyList(),
    val zones: List<LightingZone> = emptyList(),
    val topConsumers: List<DeviceEnergy> = emptyList(),
    val selectedZoneId: Long? = null,
    val selectedPeriod: EnergyPeriod = EnergyPeriod.WEEK,
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val lastUpdated: Long? = null
)

/**
 * Енергоаналітика.
 * StateFlow керує основним станом, LiveData — середньою потужністю.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class EnergyViewModel(
    private val energyRepository: EnergyRepository,
    private val deviceRepository: DeviceRepository,
    settingsRepository: ua.kpi.iot_lighting.prac2.data.settings.SettingsRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val zoneFilter = MutableStateFlow(
        savedStateHandle.get<Long>("zoneId")?.takeIf { it > 0L }
    )
    private val periodFilter = MutableStateFlow(EnergyPeriod.WEEK)
    private val refresh = MutableStateFlow(RefreshState())

    private val filters = combine(zoneFilter, periodFilter) { zoneId, period ->
        EnergyFilters(zoneId = zoneId, period = period)
    }

    private val trend = filters.flatMapLatest { currentFilters ->
        energyRepository.observeDailyTrend(
            period = currentFilters.period,
            zoneId = currentFilters.zoneId
        )
    }

    private val topConsumers = settingsRepository.settings.flatMapLatest { settings ->
        deviceRepository.observeTopConsumers(settings.tariff)
    }

    private val coreState = combine(
        energyRepository.observeSummary(),
        trend,
        deviceRepository.observeZones(),
        filters,
        refresh
    ) { summary, dailyTrend, zones, currentFilters, refreshState ->
        EnergyUiState(
            summary = summary,
            trend = dailyTrend,
            zones = zones,
            selectedZoneId = currentFilters.zoneId,
            selectedPeriod = currentFilters.period,
            isLoading = false,
            isRefreshing = refreshState.isRefreshing,
            error = refreshState.error,
            lastUpdated = refreshState.lastUpdated
        )
    }

    val uiState: StateFlow<EnergyUiState> = combine(coreState, topConsumers) { state, top ->
        state.copy(topConsumers = top)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = EnergyUiState()
    )

    /** Демонстрація LiveData поруч зі StateFlow. */
    val averagePowerLiveData: LiveData<Double> = energyRepository.observeSummary()
        .map { it.averagePowerW }
        .asLiveData()

    fun setZoneFilter(zoneId: Long?) {
        zoneFilter.value = zoneId
    }

    fun setPeriod(period: EnergyPeriod) {
        periodFilter.value = period
    }

    fun refresh() {
        if (refresh.value.isRefreshing) return
        viewModelScope.launch {
            refresh.update { it.copy(isRefreshing = true, error = null) }
            energyRepository.refresh()
                .onSuccess {
                    refresh.update {
                        it.copy(isRefreshing = false, lastUpdated = System.currentTimeMillis())
                    }
                }
                .onFailure { error ->
                    refresh.update {
                        it.copy(
                            isRefreshing = false,
                            error = error.message ?: "Не вдалося оновити показники"
                        )
                    }
                }
        }
    }

    fun clearError() {
        refresh.update { it.copy(error = null) }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val container = appContainer()
                EnergyViewModel(
                    energyRepository = container.energyRepository,
                    deviceRepository = container.deviceRepository,
                    settingsRepository = container.settingsRepository,
                    savedStateHandle = createSavedStateHandle()
                )
            }
        }
    }
}
