package com.recipecostcalculator.costing.domain.service

import com.recipecostcalculator.financial.domain.model.Money
import com.recipecostcalculator.financial.domain.model.Percentage
import com.recipecostcalculator.financial.domain.model.Quantity
import com.recipecostcalculator.ingredient.domain.model.Ingredient
import com.recipecostcalculator.ingredient.domain.model.IngredientUsageMode
import kotlin.test.Test
import kotlin.test.assertEquals

class IngredientCostCalculatorTest {
    private val calculator = IngredientCostCalculator()

    @Test
    fun calculate_byUsageMode_calculatesCorrectly() {
        val ingredient = Ingredient(
            id = 1L,
            name = "Harina",
            purchaseUnit = "Kg",
            purchasePrice = Money.of(18500.0),
            contentAmount = Quantity.of(25.0),
            usageUnit = "Kg",
        )
        val mode = IngredientUsageMode.ByUsage(Quantity.of(0.3))
        val waste = Percentage.ZERO

        val result = calculator.calculate(ingredient, mode, waste)

        assertEquals(222.0, result.amount, 1.0)
    }

    @Test
    fun calculate_byYieldMode_calculatesCorrectly() {
        val ingredient = Ingredient(
            id = 2L,
            name = "Salsa",
            purchaseUnit = "Lata",
            purchasePrice = Money.of(930.0),
            contentAmount = Quantity.of(1.0),
            usageUnit = "Lata",
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
            purchaseUnit = "Kg",
            purchasePrice = Money.of(100.0),
            contentAmount = Quantity.of(10.0),
            usageUnit = "Kg",
        )
        val mode = IngredientUsageMode.ByUsage(Quantity.of(1.0))
        val waste = Percentage.fromPercent(10.0)

        val result = calculator.calculate(ingredient, mode, waste)

        assertEquals(11.0, result.amount, 0.1)
    }

    @Test
    fun formatUsageDescription_byUsage_showsAmountAndUnit() {
        val mode = IngredientUsageMode.ByUsage(Quantity.of(0.3))

        val result = calculator.formatUsageDescription(mode, "Kg")

        assertEquals("0.3 Kg", result)
    }

    @Test
    fun formatUsageDescription_byYield_showsYieldPizzas() {
        val mode = IngredientUsageMode.ByYield(6)

        val result = calculator.formatUsageDescription(mode, "g")

        assertEquals("rinde 6 pizzas", result)
    }
}