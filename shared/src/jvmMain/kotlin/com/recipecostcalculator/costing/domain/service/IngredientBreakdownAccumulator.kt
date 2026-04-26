package com.recipecostcalculator.costing.domain.service

import com.recipecostcalculator.financial.domain.model.Money
import com.recipecostcalculator.recipe.domain.model.Quantity

data class IngredientBreakdownAccumulator(
    val ingredientId: Long,
    val ingredientName: String,
    val quantity: Quantity,
    val cost: Money,
)
