package com.recipecostcalculator.costing.domain.service

import com.recipecostcalculator.financial.domain.model.Money

data class CostResult(
    val recipeId: Long,
    val recipeName: String,
    val totalCost: Money,
    val costPerUnit: Money,
    val breakdown: List<IngredientCostBreakdown>,
)
