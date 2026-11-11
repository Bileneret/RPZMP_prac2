package ua.kpi.iot_lighting.prac2.ui.screens.about

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ua.kpi.iot_lighting.prac2.ui.components.SectionHeader
import ua.kpi.iot_lighting.prac2.ui.components.ThinDivider
import ua.kpi.iot_lighting.prac2.ui.theme.AmberLightPrimary
import ua.kpi.iot_lighting.prac2.ui.theme.OnBackgroundDark
import ua.kpi.iot_lighting.prac2.ui.theme.OnPrimaryDark
import ua.kpi.iot_lighting.prac2.ui.theme.TextMuted

private const val GITHUB_URL = "https://github.com/Bileneret/RPZMP_prac2"

private data class Feature(
    val icon: ImageVector,
    val title: String,
    val description: String
)

/** Інформація про застосунок і технології. */
@Composable
fun AboutScreen() {
    val uriHandler = LocalUriHandler.current
    val features = listOf(
        Feature(
            icon = Icons.Default.Lightbulb,
            title = "Керування світильниками",
            description = "Увімкнення, вимкнення та параметри"
        ),
        Feature(
            icon = Icons.Default.Edit,
            title = "Форми й перевірка",
            description = "Валідація та підказки"
        ),
        Feature(
            icon = Icons.Default.Bolt,
            title = "Енергоаналітика",
            description = "Споживання, вартість і графіки"
        ),
        Feature(
            icon = Icons.Default.History,
            title = "Історія",
            description = "Локальна база та фільтри"
        )
    )

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .wrapContentWidth(Alignment.CenterHorizontally)
            .widthIn(max = 680.dp)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "IoT Lighting",
            style = MaterialTheme.typography.headlineMedium,
            color = AmberLightPrimary,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Система освітлення з IoT",
            style = MaterialTheme.typography.bodyMedium,
            color = TextMuted
        )

        SectionHeader("Про проєкт")
        Text(
            text = "Практична робота №2 · Варіант 13",
            style = MaterialTheme.typography.bodyMedium,
            color = OnBackgroundDark
        )
        Text(
            text = "Недяк Олександр Сергійович · група ТВ-32",
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted
        )

        SectionHeader("Можливості")
        features.forEachIndexed { index, feature ->
            FeatureRow(feature)
            if (index < features.lastIndex) ThinDivider()
        }

        SectionHeader("Технології")
        Column(modifier = Modifier.fillMaxWidth()) {
            TechRow("Мова", "Kotlin")
            ThinDivider()
            TechRow("UI", "Jetpack Compose, Material 3")
            ThinDivider()
            TechRow("Навігація", "Navigation Compose")
            ThinDivider()
            TechRow("Стан", "ViewModel, StateFlow, LiveData")
            ThinDivider()
            TechRow("Сховище", "Room, KSP")
            ThinDivider()
            TechRow("Асинхронність", "Kotlin Coroutines")
        }

        SectionHeader("Дані")
        Text(
            text = "Профіль і налаштування зберігаються локально. Демонстраційний пароль — у SharedPreferences.",
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted
        )

        Button(
            onClick = { uriHandler.openUri(GITHUB_URL) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AmberLightPrimary,
                contentColor = OnPrimaryDark
            )
        ) {
            Icon(
                imageVector = Icons.Default.Link,
                contentDescription = null,
                modifier = Modifier.size(17.dp)
            )
            Text(
                text = "GitHub-репозиторій",
                modifier = Modifier.padding(start = 6.dp),
                fontWeight = FontWeight.SemiBold
            )
        }
        Text(
            text = GITHUB_URL,
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/** Рядок можливості. */
@Composable
private fun FeatureRow(feature: Feature) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = feature.icon,
            contentDescription = null,
            tint = AmberLightPrimary,
            modifier = Modifier.size(20.dp)
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = feature.title,
                style = MaterialTheme.typography.bodyMedium,
                color = OnBackgroundDark
            )
            Text(
                text = feature.description,
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted
            )
        }
    }
}

/** Рядок технології. */
@Composable
private fun TechRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 9.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = OnBackgroundDark
        )
    }
}
