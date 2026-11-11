package ua.kpi.iot_lighting.prac2.navigation

/** Централізований опис маршрутів навігаційного графа. */
sealed class AppDestination(val route: String, val title: String) {
    data object Menu : AppDestination("menu", "Головне меню")
    data object Devices : AppDestination("devices", "Світильники")
    data object History : AppDestination("history", "Історія")
    data object Settings : AppDestination("settings", "Профіль")
    data object About : AppDestination("about", "Про застосунок")

    data object DeviceDetails : AppDestination("device/{deviceId}", "Деталі світильника") {
        fun createRoute(deviceId: Long): String = "device/$deviceId"
    }

    data object DeviceForm : AppDestination("deviceForm?deviceId={deviceId}", "Світильник") {
        fun createRoute(deviceId: Long? = null): String = "deviceForm?deviceId=${deviceId ?: -1L}"
    }

    data object Energy : AppDestination("energy?zoneId={zoneId}", "Енергомоніторинг") {
        fun createRoute(zoneId: Long? = null): String = "energy?zoneId=${zoneId ?: -1L}"
    }
}
