package com.recipecostcalculator.domain.model

data class CostBreakdown(
    val recipeId: Long,
    val recipeName: String,
    val ingredientCostFromParent: Double,
    val ingredientCostOwn: Double,
    val totalIngredientCost: Double,
    val additionalVariableCost: Double,
    val totalVariableCost: Double,
    val fixedCostPerUnit: Double,
    val totalCostPerUnit: Double,
    val ingredientBreakdown: List<IngredientCostLine>
)

data class IngredientCostLine(
    val ingredientName: String,
    val usageDescription: String,
    val costPerPizza: Double,
    val isFromParentRecipe: Boolean
)