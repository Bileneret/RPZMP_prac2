package ua.kpi.iot_lighting.prac2.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import ua.kpi.iot_lighting.prac2.domain.model.EnergySummary
import ua.kpi.iot_lighting.prac2.domain.repository.EnergyRepository

data class MainMenuUiState(
    val summary: EnergySummary = EnergySummary(),
    val isLoading: Boolean = true
)

/**
 * ViewModel головного меню.
 * Живі статистики надходять із Room через Flow, тому меню
 * оновлюється після додавання/видалення світильника.
 */
class MainMenuViewModel(
    energyRepository: EnergyRepository
) : ViewModel() {

    val uiState: StateFlow<MainMenuUiState> = energyRepository.observeSummary()
        .map { summary -> MainMenuUiState(summary = summary, isLoading = false) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = MainMenuUiState()
        )

    companion object {
        val Factory = viewModelFactory {
            initializer {
                MainMenuViewModel(appContainer().energyRepository)
            }
        }
    }
}
