package ua.kpi.iot_lighting.prac2.domain.calculator

import ua.kpi.iot_lighting.prac2.domain.validation.toDoubleFlexible

data class LightingInput(
    val oldLampsCount: Int,
    val newLampsCount: Int,
    val oldPowerW: Double,
    val newPowerW: Double,
    val hoursPerDay: Double,
    val tariff: Double
)

data class LightingCalculationResult(
    val eOldDay: Double,
    val eNewDay: Double,
    val eCurrentDay: Double,
    val eOldMonth: Double,
    val eNewMonth: Double,
    val eCurrentMonth: Double,
    val costDay: Double,
    val costMonth: Double,
    val costYear: Double,
    val deltaP: Double,
    val savedKwhDay: Double,
    val savedKwhMonth: Double,
    val savingsDay: Double,
    val savingsMonth: Double,
    val savingsYear: Double
)

/**
 * Розрахунки енергоспоживання та економії для освітлення.
 * Чистий Kotlin без Android API — легко тестується.
 */
object LightingCalculator {

    const val DEFAULT_TARIFF = 4.32

    fun calculate(input: LightingInput): LightingCalculationResult {
        val oldLamps = input.oldLampsCount.coerceAtLeast(0)
        val newLamps = input.newLampsCount.coerceAtLeast(0)
        val oldPower = input.oldPowerW.coerceAtLeast(0.0)
        val newPower = input.newPowerW.coerceAtLeast(0.0)
        val hours = input.hoursPerDay.coerceAtLeast(0.0)
        val tariff = input.tariff.coerceAtLeast(0.0)

        val eOldDay = oldLamps * oldPower * hours / 1000.0
        val eNewDay = newLamps * newPower * hours / 1000.0
        val eCurrentDay = eOldDay + eNewDay

        val eOldMonth = eOldDay * 30.0
        val eNewMonth = eNewDay * 30.0
        val eCurrentMonth = eCurrentDay * 30.0

        val costDay = eCurrentDay * tariff
        val costMonth = costDay * 30.0
        val costYear = costDay * 365.0

        val deltaP = (oldPower - newPower).coerceAtLeast(0.0)
        val savedKwhDay = oldLamps * deltaP * hours / 1000.0
        val savedKwhMonth = savedKwhDay * 30.0

        return LightingCalculationResult(
            eOldDay = eOldDay,
            eNewDay = eNewDay,
            eCurrentDay = eCurrentDay,
            eOldMonth = eOldMonth,
            eNewMonth = eNewMonth,
            eCurrentMonth = eCurrentMonth,
            costDay = costDay,
            costMonth = costMonth,
            costYear = costYear,
            deltaP = deltaP,
            savedKwhDay = savedKwhDay,
            savedKwhMonth = savedKwhMonth,
            savingsDay = savedKwhDay * tariff,
            savingsMonth = savedKwhMonth * tariff,
            savingsYear = savedKwhDay * 365.0 * tariff
        )
    }

    fun calculate(input: LightingInput, unusedTariff: Double): LightingCalculationResult =
        calculate(input.copy(tariff = unusedTariff))

    fun calculateFromStrings(
        oldLampsCount: String,
        newLampsCount: String,
        oldPowerW: String,
        newPowerW: String,
        hoursPerDay: String,
        tariff: String
    ): LightingCalculationResult = calculate(
        LightingInput(
            oldLampsCount = oldLampsCount.trim().toIntOrNull() ?: 0,
            newLampsCount = newLampsCount.trim().toIntOrNull() ?: 0,
            oldPowerW = oldPowerW.toDoubleFlexible() ?: 0.0,
            newPowerW = newPowerW.toDoubleFlexible() ?: 0.0,
            hoursPerDay = hoursPerDay.toDoubleFlexible() ?: 0.0,
            tariff = tariff.toDoubleFlexible() ?: DEFAULT_TARIFF
        )
    )

    fun dailyKwh(powerW: Double, quantity: Int, hoursPerDay: Double): Double =
        powerW * quantity * hoursPerDay / 1000.0

    fun monthlyKwh(powerW: Double, quantity: Int, hoursPerDay: Double): Double =
        dailyKwh(powerW, quantity, hoursPerDay) * 30.0

    fun monthlyCost(powerW: Double, quantity: Int, hoursPerDay: Double, tariff: Double): Double =
        monthlyKwh(powerW, quantity, hoursPerDay) * tariff

    /**
     * Економія проти лампи розжарювання тієї ж кількості годин.
     * Базове споживання за замовчуванням — 60 Вт на лампу.
     */
    fun savingsVsIncandescent(
        powerW: Double,
        quantity: Int,
        hoursPerDay: Double,
        tariff: Double,
        baselinePowerW: Double = 60.0
    ): Double {
        val savedKwhMonth = ((baselinePowerW - powerW).coerceAtLeast(0.0) * quantity * hoursPerDay * 30.0) / 1000.0
        return savedKwhMonth * tariff
    }
}
