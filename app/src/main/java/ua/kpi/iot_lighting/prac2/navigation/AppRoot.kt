package ua.kpi.iot_lighting.prac2.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import ua.kpi.iot_lighting.prac2.ui.screens.about.AboutScreen
import ua.kpi.iot_lighting.prac2.ui.screens.devices.DeviceDetailsScreen
import ua.kpi.iot_lighting.prac2.ui.screens.devices.DeviceFormScreen
import ua.kpi.iot_lighting.prac2.ui.screens.devices.DeviceListScreen
import ua.kpi.iot_lighting.prac2.ui.screens.energy.EnergyDashboardScreen
import ua.kpi.iot_lighting.prac2.ui.screens.history.HistoryScreen
import ua.kpi.iot_lighting.prac2.ui.screens.menu.MainMenuScreen
import ua.kpi.iot_lighting.prac2.ui.screens.settings.SettingsScreen
import ua.kpi.iot_lighting.prac2.ui.theme.AmberLightPrimary
import ua.kpi.iot_lighting.prac2.ui.theme.CanvasBase
import ua.kpi.iot_lighting.prac2.ui.theme.OnBackgroundDark
import ua.kpi.iot_lighting.prac2.ui.theme.SurfaceCard

/**
 * Кореневий composable: Scaffold + NavHost.
 * Тут зібрані всі маршрути та аргументи навігації.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppRoot() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val route = backStackEntry?.destination?.route

    val title = when (route) {
        AppDestination.Menu.route -> "IoT Lighting"
        AppDestination.Devices.route -> "Світильники"
        AppDestination.DeviceDetails.route -> "Світильник"
        AppDestination.DeviceForm.route -> {
            val id = backStackEntry?.arguments?.getLong("deviceId") ?: -1L
            if (id > 0L) "Редагування" else "Новий світильник"
        }
        AppDestination.Energy.route -> "Енергоаналітика"
        AppDestination.History.route -> "Історія"
        AppDestination.Settings.route -> "Профіль"
        AppDestination.About.route -> "Про застосунок"
        else -> "IoT Lighting"
    }

    val canGoBack = navController.previousBackStackEntry != null

    Scaffold(
        containerColor = CanvasBase,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    if (canGoBack) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Назад",
                                tint = AmberLightPrimary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceCard,
                    titleContentColor = OnBackgroundDark,
                    navigationIconContentColor = AmberLightPrimary
                )
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppDestination.Menu.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(AppDestination.Menu.route) {
                MainMenuScreen(
                    onOpenDevices = { navController.navigate(AppDestination.Devices.route) },
                    onOpenEnergy = { navController.navigate(AppDestination.Energy.createRoute()) },
                    onOpenHistory = { navController.navigate(AppDestination.History.route) },
                    onOpenSettings = { navController.navigate(AppDestination.Settings.route) },
                    onOpenAbout = { navController.navigate(AppDestination.About.route) }
                )
            }

            composable(AppDestination.Devices.route) {
                DeviceListScreen(
                    onAddDevice = {
                        navController.navigate(AppDestination.DeviceForm.createRoute())
                    },
                    onOpenDevice = { id ->
                        navController.navigate(AppDestination.DeviceDetails.createRoute(id))
                    }
                )
            }

            composable(
                route = AppDestination.DeviceDetails.route,
                arguments = listOf(
                    navArgument("deviceId") { type = NavType.LongType }
                )
            ) {
                DeviceDetailsScreen(
                    onEdit = { id ->
                        navController.navigate(AppDestination.DeviceForm.createRoute(id))
                    },
                    onDeleted = { navController.popBackStack() }
                )
            }

            composable(
                route = AppDestination.DeviceForm.route,
                arguments = listOf(
                    navArgument("deviceId") {
                        type = NavType.LongType
                        defaultValue = -1L
                    }
                )
            ) {
                DeviceFormScreen(
                    onSaved = { navController.popBackStack() },
                    onCancel = { navController.popBackStack() }
                )
            }

            composable(
                route = AppDestination.Energy.route,
                arguments = listOf(
                    navArgument("zoneId") {
                        type = NavType.LongType
                        defaultValue = -1L
                    }
                )
            ) {
                EnergyDashboardScreen()
            }

            composable(AppDestination.History.route) {
                HistoryScreen()
            }

            composable(AppDestination.Settings.route) {
                SettingsScreen()
            }

            composable(AppDestination.About.route) {
                AboutScreen()
            }
        }
    }
}
