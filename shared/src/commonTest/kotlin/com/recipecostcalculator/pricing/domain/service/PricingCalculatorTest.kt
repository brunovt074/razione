package com.recipecostcalculator.pricing.domain.service

import com.recipecostcalculator.financial.domain.model.Money
import com.recipecostcalculator.financial.domain.model.Percentage
import com.recipecostcalculator.pricing.domain.model.CostMode
import com.recipecostcalculator.costing.domain.model.CostBreakdown
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PricingCalculatorTest {
    private val calculator = PricingCalculator()

    @Test
    fun calculateSuggestedPrice_withValidMargin_returnsPrice() {
        val variableCost = Money.of(100.0)
        val targetMargin = Percentage(0.5)
        val commission = Percentage.ZERO

        val result = calculator.calculateSuggestedPrice(variableCost, targetMargin, commission)

        assertEquals(200.0, result.withoutCommission.amount, 1.0)
    }

    @Test
    fun calculateSuggestedPrice_withCommission_returnsWithCommission() {
        val variableCost = Money.of(100.0)
        val targetMargin = Percentage(0.5)
        val commission = Percentage.fromPercent(10.0)

        val result = calculator.calculateSuggestedPrice(variableCost, targetMargin, commission)

        assertEquals(200.0, result.withoutCommission.amount, 1.0)
        assertEquals(222.22, result.withCommission.amount, 1.0)
    }

    @Test
    fun calculateSuggestedPrice_targetMargin100Percent_returnsZero() {
        val variableCost = Money.of(100.0)
        val targetMargin = Percentage(1.0)
        val commission = Percentage.ZERO

        val result = calculator.calculateSuggestedPrice(variableCost, targetMargin, commission)

        assertEquals(0.0, result.withoutCommission.amount, 0.01)
    }

    @Test
    fun calculateSuggestedPrice_commission100Percent_returnsZero() {
        val variableCost = Money.of(100.0)
        val targetMargin = Percentage(0.5)
        val commission = Percentage(1.0)

        val result = calculator.calculateSuggestedPrice(variableCost, targetMargin, commission)

        assertEquals(0.0, result.withCommission.amount, 0.01)
    }

    @Test
    fun analyze_withVariableCostMode_usesVariableCost() {
        val breakdown = CostBreakdown(
            recipeId = 1L,
            recipeName = "Test",
            ingredientCostFromParent = Money.ZERO,
            ingredientCostOwn = Money.of(200.0),
            totalIngredientCost = Money.of(200.0),
            additionalVariableCost = Money.ZERO,
            totalVariableCost = Money.of(200.0),
            fixedCostPerUnit = Money.of(50.0),
            totalCostPerUnit = Money.of(250.0),
            batchCost = Money.of(1200.0),
            ingredientLines = emptyList(),
        )

        val result = calculator.analyze(
            breakdown = breakdown,
            salePrice = Money.of(400.0),
            commissionPct = Percentage.ZERO,
            targetMargin = Percentage(0.5),
            costMode = CostMode.VARIABLE,
        )

        assertEquals(Money.of(200.0), result.chosenCost)
    }

    @Test
    fun analyze_withTotalCostMode_usesTotalCost() {
        val breakdown = CostBreakdown(
            recipeId = 1L,
            recipeName = "Test",
            ingredientCostFromParent = Money.ZERO,
            ingredientCostOwn = Money.of(200.0),
            totalIngredientCost = Money.of(200.0),
            additionalVariableCost = Money.ZERO,
            totalVariableCost = Money.of(200.0),
            fixedCostPerUnit = Money.of(50.0),
            totalCostPerUnit = Money.of(250.0),
            batchCost = Money.of(1200.0),
            ingredientLines = emptyList(),
        )

        val result = calculator.analyze(
            breakdown = breakdown,
            salePrice = Money.of(400.0),
            commissionPct = Percentage.ZERO,
            targetMargin = Percentage(0.5),
            costMode = CostMode.TOTAL,
        )

        assertEquals(Money.of(250.0), result.chosenCost)
    }

    @Test
    fun analyze_calculatesGrossProfitCorrectly() {
        val breakdown = CostBreakdown(
            recipeId = 1L,
            recipeName = "Test",
            ingredientCostFromParent = Money.ZERO,
            ingredientCostOwn = Money.of(200.0),
            totalIngredientCost = Money.of(200.0),
            additionalVariableCost = Money.ZERO,
            totalVariableCost = Money.of(200.0),
            fixedCostPerUnit = Money.ZERO,
            totalCostPerUnit = Money.of(200.0),
            batchCost = Money.ZERO,
            ingredientLines = emptyList(),
        )

        val result = calculator.analyze(
            breakdown = breakdown,
            salePrice = Money.of(350.0),
            commissionPct = Percentage.ZERO,
            targetMargin = Percentage(0.5),
            costMode = CostMode.VARIABLE,
        )

        assertEquals(Money.of(150.0), result.grossProfit)
    }

    @Test
    fun analyze_withCommission_subtractsCommission() {
        val breakdown = CostBreakdown(
            recipeId = 1L,
            recipeName = "Test",
            ingredientCostFromParent = Money.ZERO,
            ingredientCostOwn = Money.of(100.0),
            totalIngredientCost = Money.of(100.0),
            additionalVariableCost = Money.ZERO,
            totalVariableCost = Money.of(100.0),
            fixedCostPerUnit = Money.ZERO,
            totalCostPerUnit = Money.of(100.0),
            batchCost = Money.ZERO,
            ingredientLines = emptyList(),
        )

        val result = calculator.analyze(
            breakdown = breakdown,
            salePrice = Money.of(200.0),
            commissionPct = Percentage.fromPercent(10.0),
            targetMargin = Percentage(0.5),
            costMode = CostMode.VARIABLE,
        )

        assertEquals(Money.of(20.0), result.commissionAmount)
        assertEquals(Money.of(180.0), result.netIncome)
    }

    @Test
    fun analyze_zeroNetIncome_returnsNullGrossMargin() {
        val breakdown = CostBreakdown(
            recipeId = 1L,
            recipeName = "Test",
            ingredientCostFromParent = Money.ZERO,
            ingredientCostOwn = Money.of(200.0),
            totalIngredientCost = Money.of(200.0),
            additionalVariableCost = Money.ZERO,
            totalVariableCost = Money.of(200.0),
            fixedCostPerUnit = Money.ZERO,
            totalCostPerUnit = Money.of(200.0),
            batchCost = Money.ZERO,
            ingredientLines = emptyList(),
        )

        val result = calculator.analyze(
            breakdown = breakdown,
            salePrice = Money.ZERO,
            commissionPct = Percentage.ZERO,
            targetMargin = Percentage(0.5),
            costMode = CostMode.VARIABLE,
        )

        assertNull(result.grossMargin)
    }
}