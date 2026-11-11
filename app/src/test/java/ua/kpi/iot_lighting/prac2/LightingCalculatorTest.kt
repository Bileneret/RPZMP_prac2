package ua.kpi.iot_lighting.prac2

import org.junit.Assert.assertEquals
import org.junit.Test
import ua.kpi.iot_lighting.prac2.domain.calculator.LightingCalculator
import ua.kpi.iot_lighting.prac2.domain.calculator.LightingInput

class LightingCalculatorTest {

    private val input = LightingInput(
        oldLampsCount = 4,
        newLampsCount = 1,
        oldPowerW = 75.0,
        newPowerW = 10.0,
        hoursPerDay = 6.0,
        tariff = 4.32
    )

    @Test
    fun currentConsumptionAndCosts() {
        val result = LightingCalculator.calculate(input)

        assertEquals(1.8, result.eOldDay, 0.0001)
        assertEquals(0.06, result.eNewDay, 0.0001)
        assertEquals(1.86, result.eCurrentDay, 0.0001)
        assertEquals(54.0, result.eOldMonth, 0.0001)
        assertEquals(1.8, result.eNewMonth, 0.0001)
        assertEquals(55.8, result.eCurrentMonth, 0.0001)

        assertEquals(8.0352, result.costDay, 0.0001)
        assertEquals(241.056, result.costMonth, 0.0001)
        assertEquals(2932.848, result.costYear, 0.0001)
    }

    @Test
    fun savingsCalculation() {
        val result = LightingCalculator.calculate(input)

        assertEquals(65.0, result.deltaP, 0.0001)
        assertEquals(1.56, result.savedKwhDay, 0.0001)
        assertEquals(46.8, result.savedKwhMonth, 0.0001)
        assertEquals(6.7392, result.savingsDay, 0.0001)
        assertEquals(202.176, result.savingsMonth, 0.0001)
        assertEquals(2459.808, result.savingsYear, 0.0001)
    }

    @Test
    fun stringParsingWithComma() {
        val result = LightingCalculator.calculateFromStrings(
            oldLampsCount = " 4 ",
            newLampsCount = "1",
            oldPowerW = "75,0",
            newPowerW = "10,0",
            hoursPerDay = "6,0",
            tariff = "4,32"
        )
        assertEquals(1.86, result.eCurrentDay, 0.0001)
        assertEquals(241.056, result.costMonth, 0.0001)
    }

    @Test
    fun negativeValuesAreClamped() {
        val result = LightingCalculator.calculate(
            LightingInput(
                oldLampsCount = -4,
                newLampsCount = -1,
                oldPowerW = -75.0,
                newPowerW = -10.0,
                hoursPerDay = -6.0,
                tariff = -4.32
            )
        )
        assertEquals(0.0, result.eCurrentDay, 0.0001)
        assertEquals(0.0, result.costMonth, 0.0001)
        assertEquals(0.0, result.savingsMonth, 0.0001)
    }

    @Test
    fun deviceHelpersMatchFormula() {
        val daily = LightingCalculator.dailyKwh(powerW = 24.0, quantity = 2, hoursPerDay = 5.0)
        assertEquals(0.24, daily, 0.0001)
        assertEquals(7.2, LightingCalculator.monthlyKwh(24.0, 2, 5.0), 0.0001)
        assertEquals(31.104, LightingCalculator.monthlyCost(24.0, 2, 5.0, 4.32), 0.0001)
    }

    @Test
    fun savingsVsIncandescent() {
        val savings = LightingCalculator.savingsVsIncandescent(
            powerW = 10.0,
            quantity = 2,
            hoursPerDay = 5.0,
            tariff = 4.32
        )
        assertEquals(64.8, savings, 0.0001)
    }
}
