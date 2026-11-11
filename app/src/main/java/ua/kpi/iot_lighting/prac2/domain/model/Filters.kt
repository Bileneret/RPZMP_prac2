package ua.kpi.iot_lighting.prac2.domain.model

/** Варіанти сортування списку світильників. */
enum class DeviceSort(val label: String) {
    NAME("За назвою"),
    POWER_DESC("За потужністю"),
    NEWEST("Спочатку нові")
}

/** Фільтр списку світильників. */
data class DeviceFilter(
    val query: String = "",
    val zoneId: Long? = null,
    val type: DeviceType? = null,
    val onlyPoweredOn: Boolean = false,
    val sort: DeviceSort = DeviceSort.NAME
)

/** Варіанти сортування історії. */
enum class HistorySort(val label: String) {
    NEWEST("Нові"),
    ENERGY_DESC("За енергією"),
    COST_DESC("За вартістю")
}

/** Фільтр історії енергоспоживання. */
data class HistoryFilter(
    val zoneId: Long? = null,
    val deviceId: Long? = null,
    val sort: HistorySort = HistorySort.NEWEST
)
