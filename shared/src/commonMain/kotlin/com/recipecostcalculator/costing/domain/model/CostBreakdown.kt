package com.recipecostcalculator.costing.domain.model

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
    val ingredientLines: List<IngredientCostLine>,
)