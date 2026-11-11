package ua.kpi.iot_lighting.prac2.ui.screens.devices

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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ua.kpi.iot_lighting.prac2.domain.model.EnergyReading
import ua.kpi.iot_lighting.prac2.ui.components.ConfirmDialog
import ua.kpi.iot_lighting.prac2.ui.components.EmptyState
import ua.kpi.iot_lighting.prac2.ui.components.InfoBanner
import ua.kpi.iot_lighting.prac2.ui.components.LoadingState
import ua.kpi.iot_lighting.prac2.ui.components.PowerRow
import ua.kpi.iot_lighting.prac2.ui.components.SectionHeader
import ua.kpi.iot_lighting.prac2.ui.components.ThinDivider
import ua.kpi.iot_lighting.prac2.ui.theme.AmberLightPrimary
import ua.kpi.iot_lighting.prac2.ui.theme.BorderSlate
import ua.kpi.iot_lighting.prac2.ui.theme.CoralAlert
import ua.kpi.iot_lighting.prac2.ui.theme.OnBackgroundDark
import ua.kpi.iot_lighting.prac2.ui.theme.SurfaceCard
import ua.kpi.iot_lighting.prac2.ui.theme.TextMuted
import ua.kpi.iot_lighting.prac2.ui.util.formatDate
import ua.kpi.iot_lighting.prac2.ui.util.formatKwh
import ua.kpi.iot_lighting.prac2.ui.util.formatMoney
import ua.kpi.iot_lighting.prac2.ui.util.formatNumber
import ua.kpi.iot_lighting.prac2.ui.util.formatPower
import ua.kpi.iot_lighting.prac2.ui.viewmodel.DeviceDetailsViewModel

/** Деталі світильника: живлення, параметри, енергія та історія. */
@Composable
fun DeviceDetailsScreen(
    onEdit: (Long) -> Unit,
    onDeleted: () -> Unit,
    viewModel: DeviceDetailsViewModel = viewModel(factory = DeviceDetailsViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isDeleted) {
        if (uiState.isDeleted) onDeleted()
    }

    when {
        uiState.isLoading -> LoadingState()
        uiState.details == null -> EmptyState(
            icon = Icons.Default.Lightbulb,
            title = "Світильник не знайдено",
            message = "Можливо, його було видалено. Поверніться до списку."
        )
        else -> {
            val details = uiState.details ?: return
            val device = details.device
            val monthlyCost = device.monthlyCost(uiState.tariff)

            LazyColumn(
                modifier = Modifier
                    .fillMaxHeight()
                    .wrapContentWidth(Alignment.CenterHorizontally)
                    .widthIn(max = 720.dp),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (uiState.message != null) {
                    item {
                        InfoBanner(
                            message = uiState.message.orEmpty(),
                            onDismiss = viewModel::onMessageShown,
                            isError = true
                        )
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                        border = BorderStroke(1.dp, BorderSlate)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 16.dp, end = 4.dp, top = 14.dp, bottom = 6.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        text = device.name,
                                        style = MaterialTheme.typography.titleLarge,
                                        color = OnBackgroundDark,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "${details.zoneName} · ${device.type.label}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextMuted
                                    )
                                }
                                IconButton(onClick = { onEdit(device.id) }) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Редагувати",
                                        tint = TextMuted
                                    )
                                }
                            }
                            ThinDivider()
                            PowerRow(
                                isPoweredOn = device.isPoweredOn,
                                onCheckedChange = viewModel::setPower,
                                title = "Живлення",
                                description = if (device.isPoweredOn) {
                                    "Світильник увімкнено"
                                } else {
                                    "Світильник вимкнено"
                                }
                            )
                        }
                    }
                }

                item {
                    SectionHeader("Параметри")
                }

                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        ValueRow("Потужність", formatPower(device.powerW))
                        ThinDivider()
                        ValueRow("Яскравість", "${device.brightnessPercent} %")
                        ThinDivider()
                        ValueRow("Колірна температура", "${device.colorTempK} K")
                        ThinDivider()
                        ValueRow("Години на добу", "${formatNumber(device.hoursPerDay)} год")
                        ThinDivider()
                        ValueRow("Кількість", "${device.quantity} шт.")
                    }
                }

                item {
                    SectionHeader("Енергія")
                }

                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        ValueRow("Добова енергія", "${formatKwh(device.dailyKwh)} кВт·год")
                        ThinDivider()
                        ValueRow("Енергія за 30 днів", "${formatKwh(device.monthlyKwh)} кВт·год")
                        ThinDivider()
                        ValueRow("Вартість за 30 днів", formatMoney(monthlyCost), AmberLightPrimary)
                    }
                }

                item {
                    SectionHeader("Останні показники")
                }

                if (details.readings.isEmpty()) {
                    item {
                        Text(
                            text = "Показників ще немає.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                } else {
                    items(details.readings.take(6).size) { index ->
                        val reading = details.readings[index]
                        ReadingRow(reading)
                        if (index < details.readings.take(6).lastIndex) ThinDivider()
                    }
                }

                item {
                    TextButton(
                        onClick = { showDeleteDialog = true },
                        enabled = !uiState.isDeleting,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            tint = CoralAlert,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Видалити світильник",
                            modifier = Modifier.padding(start = 6.dp),
                            color = CoralAlert
                        )
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        ConfirmDialog(
            title = "Видалити світильник?",
            message = "Разом із пристроєм буде видалено всю історію його показників.",
            confirmLabel = "Видалити",
            onConfirm = {
                showDeleteDialog = false
                viewModel.delete()
            },
            onDismiss = { showDeleteDialog = false }
        )
    }
}

/** Рядок «підпис — значення» для секцій деталей. */
@Composable
private fun ValueRow(
    label: String,
    value: String,
    valueColor: Color = OnBackgroundDark
) {
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
            color = valueColor,
            fontWeight = FontWeight.Medium
        )
    }
}

/** Один запис з історії показників. */
@Composable
private fun ReadingRow(reading: EnergyReading) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = formatDate(reading.timestamp),
                style = MaterialTheme.typography.bodySmall,
                color = OnBackgroundDark
            )
            Text(
                text = "${formatKwh(reading.energyKwh)} кВт·год · ${formatMoney(reading.cost)}",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted
            )
        }
        Text(
            text = reading.source.label,
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted
        )
    }
}
