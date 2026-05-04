package com.recipecostcalculator.domain.model

import com.recipecostcalculator.financial.domain.model.Money

data class CostBreakdown(
    val recipeId: Long,
    val recipeName: String,
    val ingredientCostFromParent: Money,
    val ingredientCostOwn: Money,
    val totalIngredientCost: Money,
    val additionalVariableCost: Money,
    val totalVariableCost: Money,
    val fixedCostPerUnit: Money,
    val totalCostPerUnit: Money,
    val batchCost: Money,
    val ingredientBreakdown: List<IngredientCostLine>
)

data class IngredientCostLine(
    val ingredientId: Long,
    val ingredientName: String,
    val usageDescription: String,
    val costPerPizza: Money,
    val isFromParentRecipe: Boolean,
    val parentRecipeName: String? = null
)