package ua.kpi.iot_lighting.prac2.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val MidnightSlateColorScheme = darkColorScheme(
    primary = AmberLightPrimary,
    onPrimary = OnPrimaryDark,
    primaryContainer = SurfaceCardSecondary,
    onPrimaryContainer = AmberLightPrimary,
    secondary = AmberLightPrimary,
    onSecondary = OnPrimaryDark,
    secondaryContainer = SurfaceCardSecondary,
    onSecondaryContainer = AmberLightPrimary,
    tertiary = EmeraldEco,
    onTertiary = OnPrimaryDark,
    tertiaryContainer = SurfaceCardSecondary,
    onTertiaryContainer = EmeraldEco,
    background = CanvasBase,
    onBackground = OnBackgroundDark,
    surface = SurfaceCard,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceCardSecondary,
    onSurfaceVariant = TextMuted,
    outline = BorderSlate,
    error = CoralAlert,
    onError = OnPrimaryDark
)

@Composable
fun IoTLightingTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            window.statusBarColor = CanvasBase.toArgb()
            window.navigationBarColor = SurfaceCard.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = MidnightSlateColorScheme,
        typography = Typography,
        content = content
    )
}
