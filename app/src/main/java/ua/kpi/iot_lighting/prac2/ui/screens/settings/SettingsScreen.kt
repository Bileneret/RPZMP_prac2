package ua.kpi.iot_lighting.prac2.ui.screens.settings

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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ua.kpi.iot_lighting.prac2.domain.validation.SettingsField
import ua.kpi.iot_lighting.prac2.ui.components.FormTextField
import ua.kpi.iot_lighting.prac2.ui.components.InfoBanner
import ua.kpi.iot_lighting.prac2.ui.components.LoadingState
import ua.kpi.iot_lighting.prac2.ui.components.SectionHeader
import ua.kpi.iot_lighting.prac2.ui.components.ThinDivider
import ua.kpi.iot_lighting.prac2.ui.theme.AmberLightPrimary
import ua.kpi.iot_lighting.prac2.ui.theme.OnBackgroundDark
import ua.kpi.iot_lighting.prac2.ui.theme.OnPrimaryDark
import ua.kpi.iot_lighting.prac2.ui.theme.TextMuted
import ua.kpi.iot_lighting.prac2.ui.viewmodel.SettingsViewModel

/** Профіль, тариф і безпека. */
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    LaunchedEffect(uiState.message, uiState.errors) {
        if (uiState.message != null || uiState.errors.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }

    if (uiState.isLoading) {
        LoadingState(message = "Завантаження налаштувань…")
        return
    }

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxHeight()
            .wrapContentWidth(Alignment.CenterHorizontally)
            .widthIn(max = 680.dp),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (uiState.message != null) {
            item {
                InfoBanner(
                    message = uiState.message.orEmpty(),
                    onDismiss = viewModel::onMessageShown
                )
            }
        }
        if (uiState.errors.isNotEmpty()) {
            item {
                InfoBanner(
                    message = "Перевірте виділені поля.",
                    isError = true
                )
            }
        }

        item {
            SectionHeader("Особисті дані")
        }
        item {
            FormTextField(
                label = "Ім'я",
                value = uiState.name,
                onValueChange = viewModel::onNameChange,
                error = uiState.errors[SettingsField.NAME],
                placeholder = "Недяк Олександр",
                helperText = "2–50 символів",
                imeAction = ImeAction.Next
            )
        }
        item {
            FormTextField(
                label = "Email",
                value = uiState.email,
                onValueChange = viewModel::onEmailChange,
                error = uiState.errors[SettingsField.EMAIL],
                placeholder = "student@kpi.ua",
                keyboardType = KeyboardType.Email,
                helperText = "Використовується як логін",
                imeAction = ImeAction.Next
            )
        }

        item {
            SectionHeader("Енергетика")
        }
        item {
            FormTextField(
                label = "Тариф",
                value = uiState.tariff,
                onValueChange = viewModel::onTariffChange,
                error = uiState.errors[SettingsField.TARIFF],
                suffix = "₴/кВт·год",
                keyboardType = KeyboardType.Decimal,
                helperText = "Використовується в розрахунках вартості",
                imeAction = ImeAction.Next
            )
        }

        item {
            SectionHeader("Безпека")
        }
        item {
            Text(
                text = "Залиште поля порожніми, щоб не змінювати пароль.",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted
            )
        }
        item {
            FormTextField(
                label = "Поточний пароль",
                value = uiState.currentPassword,
                onValueChange = viewModel::onCurrentPasswordChange,
                error = uiState.errors[SettingsField.CURRENT_PASSWORD],
                visualTransformation = PasswordVisualTransformation(),
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next
            )
        }
        item {
            FormTextField(
                label = "Новий пароль",
                value = uiState.newPassword,
                onValueChange = viewModel::onNewPasswordChange,
                error = uiState.errors[SettingsField.NEW_PASSWORD],
                visualTransformation = PasswordVisualTransformation(),
                keyboardType = KeyboardType.Password,
                helperText = "Мінімум 8 символів, літери та цифри",
                imeAction = ImeAction.Next
            )
        }
        item {
            FormTextField(
                label = "Підтвердження пароля",
                value = uiState.confirmPassword,
                onValueChange = viewModel::onConfirmPasswordChange,
                error = uiState.errors[SettingsField.CONFIRM_PASSWORD],
                visualTransformation = PasswordVisualTransformation(),
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            )
        }
        item {
            Text(
                text = "Демонстраційний профіль: дані зберігаються локально.",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted
            )
        }

        item {
            SectionHeader("Демонстрація")
        }
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "Симулювати помилку оновлення",
                        style = MaterialTheme.typography.bodyLarge,
                        color = OnBackgroundDark
                    )
                    Text(
                        text = "Показує стан помилки на екрані аналітики",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }
                Switch(
                    checked = uiState.simulateFailure,
                    onCheckedChange = viewModel::onSimulateFailureChange
                )
            }
        }

        item {
            ThinDivider(modifier = Modifier.padding(top = 6.dp))
        }

        item {
            Button(
                onClick = viewModel::save,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AmberLightPrimary,
                    contentColor = OnPrimaryDark
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = null,
                    modifier = Modifier.size(17.dp)
                )
                Text(
                    text = "Зберегти",
                    modifier = Modifier.padding(start = 6.dp),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
