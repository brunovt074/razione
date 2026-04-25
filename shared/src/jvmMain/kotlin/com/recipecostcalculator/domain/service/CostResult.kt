package com.recipecostcalculator.domain.service

import com.recipecostcalculator.domain.model.valueobject.Money
import com.recipecostcalculator.domain.model.valueobject.Quantity

data class IngredientCostBreakdown(
    val ingredientId: Long,
    val ingredientName: String,
    val quantity: Quantity,
    val cost: Money,
)

data class CostResult(
    val recipeId: Long,
    val recipeName: String,
    val totalCost: Money,
    val costPerUnit: Money,
    val breakdown: List<IngredientCostBreakdown>,
)
