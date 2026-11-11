package ua.kpi.iot_lighting.prac2.ui.screens.menu

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ua.kpi.iot_lighting.prac2.ui.components.LoadingState
import ua.kpi.iot_lighting.prac2.ui.components.MetricCell
import ua.kpi.iot_lighting.prac2.ui.components.NavigationRow
import ua.kpi.iot_lighting.prac2.ui.components.SectionHeader
import ua.kpi.iot_lighting.prac2.ui.components.ThinDivider
import ua.kpi.iot_lighting.prac2.ui.theme.BorderSlate
import ua.kpi.iot_lighting.prac2.ui.theme.SurfaceCard
import ua.kpi.iot_lighting.prac2.ui.theme.TextMuted
import ua.kpi.iot_lighting.prac2.ui.util.formatKwh
import ua.kpi.iot_lighting.prac2.ui.util.formatMoney
import ua.kpi.iot_lighting.prac2.ui.viewmodel.MainMenuViewModel

private data class MenuEntry(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

/** Головний екран: стан системи та розділи. */
@Composable
fun MainMenuScreen(
    onOpenDevices: () -> Unit,
    onOpenEnergy: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenAbout: () -> Unit,
    viewModel: MainMenuViewModel = viewModel(factory = MainMenuViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.isLoading) {
        LoadingState(message = "Завантаження системи…")
        return
    }

    val entries = listOf(
        MenuEntry(
            title = "Світильники",
            subtitle = "${uiState.summary.totalDevices} пристроїв · ${uiState.summary.poweredOnDevices} увімкнено",
            icon = Icons.Default.Lightbulb,
            onClick = onOpenDevices
        ),
        MenuEntry(
            title = "Енергоаналітика",
            subtitle = "Споживання та вартість",
            icon = Icons.Default.Bolt,
            onClick = onOpenEnergy
        ),
        MenuEntry(
            title = "Історія",
            subtitle = "Показники з локальної бази",
            icon = Icons.Default.History,
            onClick = onOpenHistory
        ),
        MenuEntry(
            title = "Профіль",
            subtitle = "Тариф і налаштування",
            icon = Icons.Default.Person,
            onClick = onOpenSettings
        ),
        MenuEntry(
            title = "Про застосунок",
            subtitle = "Версія, автор і стек",
            icon = Icons.Default.Info,
            onClick = onOpenAbout
        )
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxHeight()
            .wrapContentWidth(Alignment.CenterHorizontally)
            .widthIn(max = 720.dp),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Система освітлення з IoT",
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                border = BorderStroke(1.dp, BorderSlate)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MetricCell(
                        label = "Увімкнено",
                        value = "${uiState.summary.poweredOnDevices} з ${uiState.summary.totalDevices}",
                        modifier = Modifier.weight(1f)
                    )
                    VerticalDivider(
                        modifier = Modifier.size(width = 1.dp, height = 36.dp),
                        color = BorderSlate
                    )
                    MetricCell(
                        label = "Добове",
                        value = "${formatKwh(uiState.summary.dailyKwh)} кВт·год",
                        modifier = Modifier.weight(1f)
                    )
                    VerticalDivider(
                        modifier = Modifier.size(width = 1.dp, height = 36.dp),
                        color = BorderSlate
                    )
                    MetricCell(
                        label = "Місяць",
                        value = formatMoney(uiState.summary.monthlyCost),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        item {
            SectionHeader("Розділи")
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                border = BorderStroke(1.dp, BorderSlate)
            ) {
                Column {
                    entries.forEachIndexed { index, entry ->
                        NavigationRow(
                            icon = entry.icon,
                            title = entry.title,
                            subtitle = entry.subtitle,
                            onClick = entry.onClick
                        )
                        if (index < entries.lastIndex) {
                            ThinDivider(modifier = Modifier.padding(start = 68.dp))
                        }
                    }
                }
            }
        }
    }
}
