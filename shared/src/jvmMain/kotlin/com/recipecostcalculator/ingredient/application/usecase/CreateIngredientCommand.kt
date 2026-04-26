package com.recipecostcalculator.ingredient.application.usecase

import com.recipecostcalculator.financial.domain.model.Money
import com.recipecostcalculator.recipe.domain.model.UnitOfMeasure

data class CreateIngredientCommand(
    val name: String,
    val unit: UnitOfMeasure,
    val costPerUnit: Money,
)
