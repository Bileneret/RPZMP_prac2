package ua.kpi.iot_lighting.prac2.ui.screens.devices

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
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
import ua.kpi.iot_lighting.prac2.domain.model.DeviceFilter
import ua.kpi.iot_lighting.prac2.domain.model.DeviceSort
import ua.kpi.iot_lighting.prac2.domain.model.DeviceType
import ua.kpi.iot_lighting.prac2.domain.model.LightingDevice
import ua.kpi.iot_lighting.prac2.ui.components.ChoiceChip
import ua.kpi.iot_lighting.prac2.ui.components.EmptyState
import ua.kpi.iot_lighting.prac2.ui.components.InfoBanner
import ua.kpi.iot_lighting.prac2.ui.components.LoadingState
import ua.kpi.iot_lighting.prac2.ui.components.PowerSwitch
import ua.kpi.iot_lighting.prac2.ui.components.SearchField
import ua.kpi.iot_lighting.prac2.ui.components.ThinDivider
import ua.kpi.iot_lighting.prac2.ui.theme.AmberLightPrimary
import ua.kpi.iot_lighting.prac2.ui.theme.EmeraldEco
import ua.kpi.iot_lighting.prac2.ui.theme.OnBackgroundDark
import ua.kpi.iot_lighting.prac2.ui.theme.OnPrimaryDark
import ua.kpi.iot_lighting.prac2.ui.theme.TextMuted
import ua.kpi.iot_lighting.prac2.ui.util.formatKwh
import ua.kpi.iot_lighting.prac2.ui.util.formatNumber
import ua.kpi.iot_lighting.prac2.ui.util.formatPower
import ua.kpi.iot_lighting.prac2.ui.viewmodel.DeviceListViewModel

/** Список світильників: пошук, фільтри, живлення та перехід у деталі. */
@Composable
fun DeviceListScreen(
    onAddDevice: () -> Unit,
    onOpenDevice: (Long) -> Unit,
    viewModel: DeviceListViewModel = viewModel(factory = DeviceListViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val allOn = uiState.devices.isNotEmpty() && uiState.devices.all { it.isPoweredOn }
    val filtersActive = uiState.filter.query.isNotBlank() ||
        uiState.filter.zoneId != null ||
        uiState.filter.type != null ||
        uiState.filter.onlyPoweredOn

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .widthIn(max = 720.dp)
                .align(Alignment.TopCenter)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SearchField(
                    value = uiState.filter.query,
                    onValueChange = viewModel::onQueryChange,
                    placeholder = "Пошук за назвою"
                )

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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    ChoiceChip(
                        label = "Увімкнені",
                        selected = uiState.filter.onlyPoweredOn,
                        onClick = {
                            viewModel.onOnlyPoweredOnChange(!uiState.filter.onlyPoweredOn)
                        }
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    SortMenuButton(
                        current = uiState.filter.sort,
                        onSelected = viewModel::onSortChange
                    )
                    TypeMenuButton(
                        current = uiState.filter.type,
                        onSelected = viewModel::onTypeSelected
                    )
                }

                if (uiState.message != null) {
                    InfoBanner(
                        message = uiState.message.orEmpty(),
                        onDismiss = viewModel::onMessageShown
                    )
                }
            }

            when {
                uiState.isLoading -> LoadingState()
                uiState.devices.isEmpty() -> EmptyState(
                    icon = Icons.Default.Lightbulb,
                    title = "Нічого не знайдено",
                    message = "Змініть фільтри або додайте світильник.",
                    actionLabel = if (uiState.filter != DeviceFilter()) {
                        "Скинути фільтри"
                    } else {
                        "Додати світильник"
                    },
                    onAction = {
                        if (uiState.filter != DeviceFilter()) {
                            viewModel.resetFilters()
                        } else {
                            onAddDevice()
                        }
                    }
                )
                else -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${uiState.devices.size} світильників",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextMuted,
                            modifier = Modifier.weight(1f)
                        )
                        if (!filtersActive) {
                            TextButton(onClick = { viewModel.setAllPower(!allOn) }) {
                                Text(
                                    text = if (allOn) "Вимкнути всі" else "Увімкнути всі",
                                    color = AmberLightPrimary
                                )
                            }
                        }
                    }
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 100.dp)
                    ) {
                        items(uiState.devices, key = { it.id }) { device ->
                            DeviceRow(
                                device = device,
                                zoneName = uiState.zones.firstOrNull { it.id == device.zoneId }?.name
                                    ?: "Без зони",
                                onOpen = { onOpenDevice(device.id) },
                                onPowerChange = { isOn -> viewModel.setPower(device.id, isOn) }
                            )
                            ThinDivider(modifier = Modifier.padding(start = 16.dp))
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = onAddDevice,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = AmberLightPrimary,
            contentColor = OnPrimaryDark
        ) {
            Icon(Icons.Default.Add, contentDescription = "Додати світильник")
        }
    }
}

/** Рядок світильника: назва, зона, параметри та перемикач живлення. */
@Composable
private fun DeviceRow(
    device: LightingDevice,
    zoneName: String,
    onOpen: () -> Unit,
    onPowerChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen)
            .padding(start = 16.dp, end = 8.dp, top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = device.name,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyLarge,
                    color = OnBackgroundDark,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (device.isPoweredOn) "Увімкнено" else "Вимкнено",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (device.isPoweredOn) EmeraldEco else TextMuted
                )
            }
            Text(
                text = "$zoneName · ${device.type.label}",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${formatPower(device.powerW)} · ${formatNumber(device.hoursPerDay)} год/добу",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted
            )
        }
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "${formatKwh(device.dailyKwh)} кВт·год",
                style = MaterialTheme.typography.labelMedium,
                color = if (device.isPoweredOn) AmberLightPrimary else TextMuted
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                PowerSwitch(
                    checked = device.isPoweredOn,
                    onCheckedChange = onPowerChange
                )
            }
        }
    }
}

/** Меню сортування. */
@Composable
private fun SortMenuButton(
    current: DeviceSort,
    onSelected: (DeviceSort) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Sort,
                contentDescription = "Сортування",
                tint = TextMuted
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DeviceSort.entries.forEach { sort ->
                DropdownMenuItem(
                    text = { Text(sort.label) },
                    trailingIcon = {
                        if (sort == current) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = AmberLightPrimary
                            )
                        }
                    },
                    onClick = {
                        onSelected(sort)
                        expanded = false
                    }
                )
            }
        }
    }
}

/** Меню фільтра за типом. */
@Composable
private fun TypeMenuButton(
    current: DeviceType?,
    onSelected: (DeviceType?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(
                imageVector = Icons.Default.Tune,
                contentDescription = "Тип світильника",
                tint = if (current != null) AmberLightPrimary else TextMuted
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text("Усі типи") },
                onClick = {
                    onSelected(null)
                    expanded = false
                }
            )
            DeviceType.entries.forEach { type ->
                DropdownMenuItem(
                    text = { Text(type.label) },
                    onClick = {
                        onSelected(type)
                        expanded = false
                    }
                )
            }
        }
    }
}
