package com.recipecostcalculator.costing.domain.service

import com.recipecostcalculator.financial.domain.model.Money
import com.recipecostcalculator.financial.domain.model.Percentage
import com.recipecostcalculator.financial.domain.model.Quantity
import com.recipecostcalculator.ingredient.domain.model.Ingredient
import com.recipecostcalculator.ingredient.domain.model.IngredientUsageMode
import com.recipecostcalculator.measurement.MeasurementDimension
import com.recipecostcalculator.measurement.MeasurementUnit
import com.recipecostcalculator.measurement.UnitConverter
import kotlin.test.Test
import kotlin.test.assertEquals

class IngredientCostCalculatorTest {
    private val calculator = IngredientCostCalculator()

    private fun massQty(amount: Double, unit: MeasurementUnit) =
        Quantity(UnitConverter.toCanonical(amount, unit), MeasurementUnit.KG)

    private fun countQty(amount: Double) = Quantity(amount, MeasurementUnit.UN)

    @Test
    fun calculate_byUsageMode_calculatesCorrectly() {
        val ingredient = Ingredient(
            id = 1L,
            name = "Harina",
            dimension = MeasurementDimension.MASS,
            purchaseUnit = MeasurementUnit.KG,
            purchasePrice = Money.of(18500.0),
            contentAmount = massQty(25.0, MeasurementUnit.KG),
            usageUnit = MeasurementUnit.KG,
        )
        val mode = IngredientUsageMode.ByUsage(massQty(0.3, MeasurementUnit.KG))
        val waste = Percentage.ZERO

        val result = calculator.calculate(ingredient, mode, waste)

        assertEquals(222.0, result.amount, 1.0)
    }

    @Test
    fun calculate_byYieldMode_calculatesCorrectly() {
        val ingredient = Ingredient(
            id = 2L,
            name = "Salsa",
            dimension = MeasurementDimension.COUNT,
            purchaseUnit = MeasurementUnit.UN,
            purchasePrice = Money.of(930.0),
            contentAmount = countQty(1.0),
            usageUnit = MeasurementUnit.UN,
        )
        val mode = IngredientUsageMode.ByYield(6)
        val waste = Percentage.ZERO

        val result = calculator.calculate(ingredient, mode, waste)

        assertEquals(155.0, result.amount, 1.0)
    }

    @Test
    fun calculate_withWasteFactor_appliesMultiplier() {
        val ingredient = Ingredient(
            id = 1L,
            name = "Harina",
            dimension = MeasurementDimension.MASS,
            purchaseUnit = MeasurementUnit.KG,
            purchasePrice = Money.of(100.0),
            contentAmount = massQty(10.0, MeasurementUnit.KG),
            usageUnit = MeasurementUnit.KG,
        )
        val mode = IngredientUsageMode.ByUsage(massQty(1.0, MeasurementUnit.KG))
        val waste = Percentage.fromPercent(10.0)

        val result = calculator.calculate(ingredient, mode, waste)

        assertEquals(11.0, result.amount, 0.1)
    }

    @Test
    fun formatUsageDescription_byYield_showsYieldPizzas() {
        val mode = IngredientUsageMode.ByYield(6)

        val result = calculator.formatUsageDescription(mode, "g")

        assertEquals("rinde 6 pizzas", result)
    }
}
