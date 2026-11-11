package ua.kpi.iot_lighting.prac2.domain.model

/** Період графіка споживання. */
enum class EnergyPeriod(val label: String) {
    WEEK("7 днів"),
    TWO_WEEKS("14 днів"),
    MONTH("Місяць")
}
