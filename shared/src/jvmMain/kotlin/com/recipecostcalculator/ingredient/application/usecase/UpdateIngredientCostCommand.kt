package com.recipecostcalculator.ingredient.application.usecase

import com.recipecostcalculator.financial.domain.model.Money

data class UpdateIngredientCostCommand(
    val ingredientId: Long,
    val costPerUnit: Money,
)
