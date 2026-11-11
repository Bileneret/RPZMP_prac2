package ua.kpi.iot_lighting.prac2.ui.screens.energy

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ua.kpi.iot_lighting.prac2.domain.model.DailyEnergy
import ua.kpi.iot_lighting.prac2.domain.model.DeviceEnergy
import ua.kpi.iot_lighting.prac2.domain.model.EnergyPeriod
import ua.kpi.iot_lighting.prac2.ui.components.ChoiceChip
import ua.kpi.iot_lighting.prac2.ui.components.InfoBanner
import ua.kpi.iot_lighting.prac2.ui.components.LoadingState
import ua.kpi.iot_lighting.prac2.ui.components.MetricCell
import ua.kpi.iot_lighting.prac2.ui.components.SectionHeader
import ua.kpi.iot_lighting.prac2.ui.components.ThinDivider
import ua.kpi.iot_lighting.prac2.ui.theme.AmberLightPrimary
import ua.kpi.iot_lighting.prac2.ui.theme.BorderSlate
import ua.kpi.iot_lighting.prac2.ui.theme.OnBackgroundDark
import ua.kpi.iot_lighting.prac2.ui.theme.SurfaceCard
import ua.kpi.iot_lighting.prac2.ui.theme.TextMuted
import ua.kpi.iot_lighting.prac2.ui.util.formatDate
import ua.kpi.iot_lighting.prac2.ui.util.formatDay
import ua.kpi.iot_lighting.prac2.ui.util.formatKwh
import ua.kpi.iot_lighting.prac2.ui.util.formatMoney
import ua.kpi.iot_lighting.prac2.ui.util.formatPower
import ua.kpi.iot_lighting.prac2.ui.viewmodel.EnergyViewModel

/** Аналітика споживання: зведення, графік і найбільші споживачі. */
@Composable
fun EnergyDashboardScreen(
    viewModel: EnergyViewModel = viewModel(factory = EnergyViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val averagePower by viewModel.averagePowerLiveData.observeAsState(0.0)
    val chartTitle = when (uiState.selectedPeriod) {
        EnergyPeriod.WEEK -> "Споживання за 7 днів"
        EnergyPeriod.TWO_WEEKS -> "Споживання за 14 днів"
        EnergyPeriod.MONTH -> "Споживання за поточний місяць"
    }

    if (uiState.isLoading) {
        LoadingState(message = "Зчитування показників…")
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxHeight()
            .wrapContentWidth(Alignment.CenterHorizontally)
            .widthIn(max = 720.dp),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (uiState.error != null) {
            item {
                InfoBanner(
                    message = uiState.error.orEmpty(),
                    isError = true,
                    onDismiss = viewModel::clearError
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (uiState.lastUpdated != null) {
                        "Оновлено ${formatDate(uiState.lastUpdated!!)}"
                    } else {
                        "Ще не оновлювалося"
                    },
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
                TextButton(
                    onClick = viewModel::refresh,
                    enabled = !uiState.isRefreshing
                ) {
                    if (uiState.isRefreshing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(15.dp),
                            color = AmberLightPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            tint = AmberLightPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = if (uiState.isRefreshing) "Оновлення…" else "Оновити",
                        modifier = Modifier.padding(start = 4.dp),
                        color = AmberLightPrimary
                    )
                }
            }
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
            Text(
                text = "Поточний місяць: ${formatKwh(uiState.summary.monthlyKwh)} кВт·год · ${formatMoney(uiState.summary.monthlyCost)}",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                modifier = Modifier.padding(horizontal = 2.dp)
            )
        }

        item {
            Text(
                text = "Фільтр графіка",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                modifier = Modifier.padding(horizontal = 2.dp)
            )
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ChoiceChip(
                    label = "Усі зони",
                    selected = uiState.selectedZoneId == null,
                    onClick = { viewModel.setZoneFilter(null) }
                )
                uiState.zones.forEach { zone ->
                    ChoiceChip(
                        label = zone.name,
                        selected = uiState.selectedZoneId == zone.id,
                        onClick = { viewModel.setZoneFilter(zone.id) }
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                EnergyPeriod.entries.forEach { period ->
                    ChoiceChip(
                        label = period.label,
                        selected = uiState.selectedPeriod == period,
                        onClick = { viewModel.setPeriod(period) }
                    )
                }
            }
        }

        item {
            SectionHeader(chartTitle)
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                border = BorderStroke(1.dp, BorderSlate)
            ) {
                if (uiState.trend.isEmpty()) {
                    Text(
                        text = "Немає показників для графіка.",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                } else {
                    EnergyBarChart(
                        trend = uiState.trend,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp)
                    )
                }
            }
        }

        item {
            SectionHeader("Найбільші споживачі")
        }

        item {
            if (uiState.topConsumers.isEmpty()) {
                Text(
                    text = "Немає даних.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            } else {
                Column(modifier = Modifier.fillMaxWidth()) {
                    uiState.topConsumers.take(5).forEachIndexed { index, consumer ->
                        ConsumerRow(consumer)
                        if (index < uiState.topConsumers.take(5).lastIndex) ThinDivider()
                    }
                }
            }
        }

        item {
            SectionHeader("Технічні дані")
        }

        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                ValueRow("Середня потужність (LiveData)", formatPower(averagePower))
                ThinDivider()
                ValueRow("Записів у базі", uiState.summary.readingsCount.toString())
                ThinDivider()
                ValueRow("Середній показник", "${formatKwh(uiState.summary.averageReadingKwh)} кВт·год")
            }
        }
    }
}

/** Простий бар-чарт: значення над стовпчиком, дата під ним. */
@Composable
private fun EnergyBarChart(
    trend: List<DailyEnergy>,
    modifier: Modifier = Modifier
) {
    val max = trend.maxOf { it.totalKwh }.coerceAtLeast(0.01)
    if (trend.size <= 7) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .height(168.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            trend.forEach { item ->
                BarColumn(
                    item = item,
                    max = max,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    } else {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .height(168.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            trend.forEach { item ->
                BarColumn(
                    item = item,
                    max = max,
                    modifier = Modifier.width(30.dp)
                )
            }
        }
    }
}

/** Один стовпчик графіка. */
@Composable
private fun BarColumn(
    item: DailyEnergy,
    max: Double,
    modifier: Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        Text(
            text = formatKwh(item.totalKwh),
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted,
            maxLines = 1,
            overflow = TextOverflow.Clip
        )
        Box(
            modifier = Modifier
                .padding(top = 4.dp)
                .width(18.dp)
                .height(
                    ((item.totalKwh / max * 110.0).coerceAtLeast(4.0)).toFloat().dp
                )
                .clip(RoundedCornerShape(4.dp))
                .background(AmberLightPrimary)
        )
        Text(
            text = formatDay(item.day),
            modifier = Modifier.padding(top = 6.dp),
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted,
            maxLines = 1
        )
    }
}

/** Рядок найбільшого споживача. */
@Composable
private fun ConsumerRow(consumer: DeviceEnergy) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = consumer.name,
                style = MaterialTheme.typography.bodyMedium,
                color = OnBackgroundDark,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = consumer.zoneName,
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "${formatKwh(consumer.dailyKwh)} кВт·год",
                style = MaterialTheme.typography.labelMedium,
                color = AmberLightPrimary
            )
            Text(
                text = formatMoney(consumer.monthlyCost),
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted
            )
        }
    }
}

/** Рядок технічного показника. */
@Composable
private fun ValueRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = OnBackgroundDark
        )
    }
}
