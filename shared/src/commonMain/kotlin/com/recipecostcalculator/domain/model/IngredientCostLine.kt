package com.recipecostcalculator.domain.model

import com.recipecostcalculator.financial.domain.model.Money

data class IngredientCostLine(
    val ingredientId: Long,
    val ingredientName: String,
    val usageDescription: String,
    val costPerPizza: Money,
    val isFromParentRecipe: Boolean,
    val parentRecipeName: String? = null
)
