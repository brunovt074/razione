package com.recipecostcalculator.domain.service

import com.recipecostcalculator.domain.model.Ingredient
import com.recipecostcalculator.domain.model.IngredientUsageMode
import com.recipecostcalculator.financial.domain.model.Money
import com.recipecostcalculator.financial.domain.model.Percentage
import java.math.BigDecimal

class IngredientCostCalculator {

    fun calculate(
        ingredient: Ingredient,
        mode: IngredientUsageMode,
        wasteFactor: Percentage,
    ): Money {
        val baseCost = when (mode) {
            is IngredientUsageMode.ByUsage -> {
                val pricePerUnit = ingredient.purchasePrice / ingredient.contentAmount.value.toDouble()
                pricePerUnit * mode.amountPerPizza.value.toDouble()
            }
            is IngredientUsageMode.ByYield -> {
                ingredient.purchasePrice / mode.pizzasPerPurchaseUnit
            }
        }
        val wasteMultiplier = BigDecimal.ONE.add(wasteFactor.value)
        return baseCost * wasteMultiplier.toDouble()
    }

    fun formatUsageDescription(
        mode: IngredientUsageMode,
        usageUnit: String,
    ): String = when (mode) {
        is IngredientUsageMode.ByUsage ->
            "${mode.amountPerPizza.value.toPlainString()} $usageUnit"
        is IngredientUsageMode.ByYield ->
            "rinde ${mode.pizzasPerPurchaseUnit} pizzas"
    }
}