package ua.kpi.iot_lighting.prac2.ui.screens.devices

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ua.kpi.iot_lighting.prac2.domain.model.DeviceType
import ua.kpi.iot_lighting.prac2.domain.validation.DeviceFormField
import ua.kpi.iot_lighting.prac2.ui.components.DropdownField
import ua.kpi.iot_lighting.prac2.ui.components.FormTextField
import ua.kpi.iot_lighting.prac2.ui.components.InfoBanner
import ua.kpi.iot_lighting.prac2.ui.components.LoadingState
import ua.kpi.iot_lighting.prac2.ui.components.PowerRow
import ua.kpi.iot_lighting.prac2.ui.components.SectionHeader
import ua.kpi.iot_lighting.prac2.ui.components.ThinDivider
import ua.kpi.iot_lighting.prac2.ui.theme.AmberLightPrimary
import ua.kpi.iot_lighting.prac2.ui.theme.BorderSlate
import ua.kpi.iot_lighting.prac2.ui.theme.OnBackgroundDark
import ua.kpi.iot_lighting.prac2.ui.theme.OnPrimaryDark
import ua.kpi.iot_lighting.prac2.ui.theme.SurfaceCard
import ua.kpi.iot_lighting.prac2.ui.theme.TextMuted
import ua.kpi.iot_lighting.prac2.ui.viewmodel.DeviceFormViewModel
import kotlin.math.roundToInt

/** Форма світильника: основне, енергоспоживання та стан. */
@Composable
fun DeviceFormScreen(
    onSaved: () -> Unit,
    onCancel: () -> Unit,
    viewModel: DeviceFormViewModel = viewModel(factory = DeviceFormViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) onSaved()
    }

    if (uiState.isLoading) {
        LoadingState(message = "Завантаження форми…")
        return
    }

    val selectedZone = uiState.zones.firstOrNull { it.id == uiState.zoneId }
    val brightnessValue = (uiState.brightness.toIntOrNull() ?: 0).coerceIn(0, 100).toFloat()
    val colorTempValue = (uiState.colorTempK.toIntOrNull() ?: 4000).coerceIn(2000, 6500).toFloat()
    val scrollState = rememberScrollState()

    LaunchedEffect(uiState.errors) {
        if (uiState.errors.isNotEmpty()) {
            scrollState.animateScrollTo(0)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .widthIn(max = 680.dp)
                .fillMaxHeight()
                .align(Alignment.TopCenter)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (uiState.message != null) {
                    InfoBanner(
                        message = uiState.message.orEmpty(),
                        onDismiss = viewModel::onMessageShown,
                        isError = true
                    )
                }
                if (uiState.errors.isNotEmpty()) {
                    InfoBanner(
                        message = "Перевірте виділені поля.",
                        isError = true
                    )
                }

                SectionHeader("Основне")
                FormTextField(
                    label = "Назва",
                    value = uiState.name,
                    onValueChange = viewModel::onNameChange,
                    error = uiState.errors[DeviceFormField.NAME],
                    placeholder = "Наприклад, Люстра вітальня",
                    helperText = "2–40 символів, назва має бути унікальною",
                    enabled = !uiState.isSaving,
                    imeAction = ImeAction.Next
                )
                DropdownField(
                    label = "Зона",
                    options = uiState.zones,
                    selected = selectedZone,
                    display = { it.name },
                    onSelected = { viewModel.onZoneSelected(it.id) },
                    error = uiState.errors[DeviceFormField.ZONE],
                    enabled = !uiState.isSaving
                )
                DropdownField(
                    label = "Тип світильника",
                    options = DeviceType.entries,
                    selected = uiState.type,
                    display = { it.label },
                    onSelected = viewModel::onTypeSelected,
                    enabled = !uiState.isSaving
                )

                SectionHeader("Енергоспоживання")
                FormTextField(
                    label = "Потужність",
                    value = uiState.powerW,
                    onValueChange = viewModel::onPowerChange,
                    error = uiState.errors[DeviceFormField.POWER],
                    placeholder = "24",
                    suffix = "Вт",
                    keyboardType = KeyboardType.Number,
                    helperText = "Діапазон 1–1000 Вт",
                    enabled = !uiState.isSaving
                )
                SliderField(
                    label = "Яскравість",
                    valueText = "${brightnessValue.toInt()} %",
                    value = brightnessValue,
                    valueRange = 0f..100f,
                    steps = 9,
                    onValueChange = { viewModel.onBrightnessChange(it.roundToInt().toString()) },
                    enabled = !uiState.isSaving
                )
                SliderField(
                    label = "Колірна температура",
                    valueText = "${colorTempValue.toInt()} K",
                    value = colorTempValue,
                    valueRange = 2000f..6500f,
                    steps = 8,
                    onValueChange = { viewModel.onColorTempChange(it.roundToInt().toString()) },
                    enabled = !uiState.isSaving
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FormTextField(
                        label = "Години/добу",
                        value = uiState.hoursPerDay,
                        onValueChange = viewModel::onHoursChange,
                        error = uiState.errors[DeviceFormField.HOURS],
                        suffix = "год",
                        keyboardType = KeyboardType.Number,
                        modifier = Modifier.weight(1f),
                        enabled = !uiState.isSaving
                    )
                    FormTextField(
                        label = "Кількість",
                        value = uiState.quantity,
                        onValueChange = viewModel::onQuantityChange,
                        error = uiState.errors[DeviceFormField.QUANTITY],
                        suffix = "шт.",
                        keyboardType = KeyboardType.Number,
                        modifier = Modifier.weight(1f),
                        enabled = !uiState.isSaving
                    )
                }

                SectionHeader("Стан")
                PowerRow(
                    isPoweredOn = uiState.isPoweredOn,
                    onCheckedChange = viewModel::onPowerChange,
                    enabled = !uiState.isSaving
                )
            }

            Surface(
                color = SurfaceCard,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    ThinDivider()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = onCancel,
                            enabled = !uiState.isSaving
                        ) {
                            Text("Скасувати", color = TextMuted)
                        }
                        Button(
                            onClick = viewModel::save,
                            enabled = !uiState.isSaving,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AmberLightPrimary,
                                contentColor = OnPrimaryDark
                            )
                        ) {
                            if (uiState.isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = OnPrimaryDark,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Save,
                                    contentDescription = null,
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                            Text(
                                text = if (uiState.isSaving) "Збереження…" else "Зберегти",
                                modifier = Modifier.padding(start = 6.dp),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

/** Компактний слайдер із підписом і значенням. */
@Composable
private fun SliderField(
    label: String,
    valueText: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    onValueChange: (Float) -> Unit,
    enabled: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted
            )
            Text(
                text = valueText,
                style = MaterialTheme.typography.labelMedium,
                color = OnBackgroundDark
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            enabled = enabled,
            colors = SliderDefaults.colors(
                thumbColor = AmberLightPrimary,
                activeTrackColor = AmberLightPrimary,
                inactiveTrackColor = BorderSlate
            )
        )
    }
}
