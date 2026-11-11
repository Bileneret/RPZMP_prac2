package ua.kpi.iot_lighting.prac2.ui.screens.history

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ua.kpi.iot_lighting.prac2.domain.model.HistoryFilter
import ua.kpi.iot_lighting.prac2.domain.model.HistorySort
import ua.kpi.iot_lighting.prac2.domain.model.ReadingRow
import ua.kpi.iot_lighting.prac2.ui.components.ChoiceChip
import ua.kpi.iot_lighting.prac2.ui.components.ConfirmDialog
import ua.kpi.iot_lighting.prac2.ui.components.EmptyState
import ua.kpi.iot_lighting.prac2.ui.components.InfoBanner
import ua.kpi.iot_lighting.prac2.ui.components.LoadingState
import ua.kpi.iot_lighting.prac2.ui.components.ThinDivider
import ua.kpi.iot_lighting.prac2.ui.theme.AmberLightPrimary
import ua.kpi.iot_lighting.prac2.ui.theme.OnBackgroundDark
import ua.kpi.iot_lighting.prac2.ui.theme.TextMuted
import ua.kpi.iot_lighting.prac2.ui.util.formatDateOnly
import ua.kpi.iot_lighting.prac2.ui.util.formatKwh
import ua.kpi.iot_lighting.prac2.ui.util.formatMoney
import ua.kpi.iot_lighting.prac2.ui.util.formatTime
import ua.kpi.iot_lighting.prac2.ui.viewmodel.HistoryViewModel

/** Історія показників: фільтри, групування за датою та видалення. */
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = viewModel(factory = HistoryViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var readingToDelete by remember { mutableStateOf<ReadingRow?>(null) }
    var showClearDialog by remember { mutableStateOf(false) }

    if (uiState.isLoading) {
        LoadingState(message = "Зчитування історії…")
        return
    }

    val devicesForZone = uiState.devices.filter { device ->
        uiState.filter.zoneId == null || device.zoneId == uiState.filter.zoneId
    }
    val grouped = uiState.readings.groupBy { formatDateOnly(it.timestamp) }
    val hasFilters = uiState.filter != HistoryFilter()

    LazyColumn(
        modifier = Modifier
            .fillMaxHeight()
            .wrapContentWidth(Alignment.CenterHorizontally)
            .widthIn(max = 720.dp),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (uiState.message != null) {
            item {
                InfoBanner(
                    message = uiState.message.orEmpty(),
                    onDismiss = viewModel::onMessageShown
                )
            }
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
                    selected = uiState.filter.zoneId == null,
                    onClick = { viewModel.onZoneSelected(null) }
                )
                uiState.zones.forEach { zone ->
                    ChoiceChip(
                        label = zone.name,
                        selected = uiState.filter.zoneId == zone.id,
                        onClick = { viewModel.onZoneSelected(zone.id) }
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterMenuButton(
                    label = uiState.filter.deviceId?.let { id ->
                        devicesForZone.firstOrNull { it.id == id }?.name ?: "Пристрій"
                    } ?: "Усі пристрої",
                    selected = uiState.filter.deviceId != null,
                    options = buildList {
                        add("Усі пристрої" to { viewModel.onDeviceSelected(null) })
                        devicesForZone.forEach { device ->
                            add(device.name to { viewModel.onDeviceSelected(device.id) })
                        }
                    }
                )
                FilterMenuButton(
                    label = uiState.filter.sort.label,
                    selected = uiState.filter.sort != HistorySort.NEWEST,
                    options = HistorySort.entries.map { sort ->
                        sort.label to { viewModel.onSortChange(sort) }
                    }
                )
                Spacer(modifier = Modifier.weight(1f))
                if (hasFilters) {
                    IconButton(onClick = viewModel::resetFilters) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Скинути фільтри",
                            tint = TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                IconButton(onClick = { showClearDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Очистити історію",
                        tint = TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        if (uiState.readings.isEmpty()) {
            item {
                EmptyState(
                    icon = Icons.Default.History,
                    title = "Історія порожня",
                    message = if (hasFilters) {
                        "Змініть фільтри або скиньте їх."
                    } else {
                        "Показники з'являться після оновлення даних."
                    },
                    actionLabel = if (hasFilters) "Скинути фільтри" else null,
                    onAction = if (hasFilters) viewModel::resetFilters else null
                )
            }
        } else if (uiState.filter.sort == HistorySort.NEWEST) {
            grouped.forEach { (date, readings) ->
                item(key = "header-$date") {
                    Text(
                        text = date,
                        modifier = Modifier.padding(top = 10.dp, bottom = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = TextMuted,
                        fontWeight = FontWeight.Medium
                    )
                }
                items(readings, key = { it.id }) { reading ->
                    HistoryRow(
                        reading = reading,
                        showDate = false,
                        onDelete = { readingToDelete = reading }
                    )
                    ThinDivider()
                }
            }
        } else {
            items(uiState.readings, key = { it.id }) { reading ->
                HistoryRow(
                    reading = reading,
                    showDate = true,
                    onDelete = { readingToDelete = reading }
                )
                ThinDivider()
            }
        }
    }

    readingToDelete?.let { reading ->
        ConfirmDialog(
            title = "Видалити запис?",
            message = "Запис від ${formatDateOnly(reading.timestamp)} буде видалено з локальної бази.",
            confirmLabel = "Видалити",
            onConfirm = {
                viewModel.deleteReading(reading.id)
                readingToDelete = null
            },
            onDismiss = { readingToDelete = null }
        )
    }

    if (showClearDialog) {
        ConfirmDialog(
            title = "Очистити всю історію?",
            message = "Буде видалено всі записи енергоспоживання. Світильники залишаться.",
            confirmLabel = "Очистити",
            onConfirm = {
                viewModel.clearHistory()
                showClearDialog = false
            },
            onDismiss = { showClearDialog = false }
        )
    }
}

/** Компактне меню фільтра у рядку. */
@Composable
private fun FilterMenuButton(
    label: String,
    selected: Boolean,
    options: List<Pair<String, () -> Unit>>
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        FilterChip(
            selected = selected,
            onClick = { expanded = true },
            label = {
                Text(
                    text = label,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.widthIn(max = 118.dp)
                )
            },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = AmberLightPrimary.copy(alpha = 0.16f),
                selectedLabelColor = AmberLightPrimary,
                labelColor = TextMuted
            )
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { (text, action) ->
                DropdownMenuItem(
                    text = { Text(text) },
                    onClick = {
                        action()
                        expanded = false
                    }
                )
            }
        }
    }
}

/** Один запис історії. */
@Composable
private fun HistoryRow(
    reading: ReadingRow,
    showDate: Boolean,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = reading.deviceName,
                style = MaterialTheme.typography.bodyMedium,
                color = OnBackgroundDark,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = if (showDate) {
                    "${formatDateOnly(reading.timestamp)} · ${formatTime(reading.timestamp)}"
                } else {
                    "${reading.zoneName} · ${formatTime(reading.timestamp)}"
                },
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "${formatKwh(reading.energyKwh)} кВт·год",
                style = MaterialTheme.typography.labelMedium,
                color = AmberLightPrimary
            )
            Text(
                text = formatMoney(reading.cost),
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted
            )
        }
        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Видалити запис",
                tint = TextMuted,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
