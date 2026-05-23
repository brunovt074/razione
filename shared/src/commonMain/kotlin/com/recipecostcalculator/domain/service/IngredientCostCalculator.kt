package com.recipecostcalculator.domain.service

import com.recipecostcalculator.domain.model.ByUsage
import com.recipecostcalculator.domain.model.ByYield
import com.recipecostcalculator.domain.model.Ingredient
import com.recipecostcalculator.domain.model.IngredientUsageMode
import com.recipecostcalculator.financial.domain.model.Money
import com.recipecostcalculator.financial.domain.model.Percentage

class IngredientCostCalculator {

    fun calculate(
        ingredient: Ingredient,
        mode: IngredientUsageMode,
        wasteFactor: Percentage,
    ): Money {
        val baseCost = when (mode) {
            is ByUsage -> {
                val pricePerUnit = ingredient.purchasePrice / ingredient.contentAmount
                pricePerUnit * mode.amountPerPizza.value
            }
            is ByYield -> {
                ingredient.purchasePrice / mode.pizzasPerPurchaseUnit
            }
        }
        val wasteMultiplier = 1.0 + wasteFactor.value
        return baseCost * wasteMultiplier
    }

    fun formatUsageDescription(
        mode: IngredientUsageMode,
        usageUnit: String,
    ): String = when (mode) {
        is ByUsage ->
            "${mode.amountPerPizza.value} $usageUnit"
        is ByYield ->
            "rinde ${mode.pizzasPerPurchaseUnit} pizzas"
    }
}
