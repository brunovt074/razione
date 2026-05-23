package com.recipecostcalculator.costing.domain.service

import com.recipecostcalculator.financial.domain.model.Money
import com.recipecostcalculator.financial.domain.model.Percentage
import com.recipecostcalculator.ingredient.domain.model.Ingredient
import com.recipecostcalculator.ingredient.domain.model.IngredientUsageMode

class IngredientCostCalculator {
    fun calculate(
        ingredient: Ingredient,
        mode: IngredientUsageMode,
        wasteFactor: Percentage,
    ): Money {
        val baseCost = when (mode) {
            is IngredientUsageMode.ByUsage -> {
                val pricePerCanonical = ingredient.purchasePrice / ingredient.contentAmount.value
                pricePerCanonical * mode.amountPerPizza.value
            }
            is IngredientUsageMode.ByYield -> {
                ingredient.purchasePrice / mode.pizzasPerPurchaseUnit
            }
        }
        return baseCost * (1.0 + wasteFactor.toDouble())
    }

    fun formatUsageDescription(
        mode: IngredientUsageMode,
        usageUnit: String,
    ): String = when (mode) {
        is IngredientUsageMode.ByUsage ->
            "${mode.amountPerPizza.value} $usageUnit"
        is IngredientUsageMode.ByYield ->
            "rinde ${mode.pizzasPerPurchaseUnit} pizzas"
    }
}
