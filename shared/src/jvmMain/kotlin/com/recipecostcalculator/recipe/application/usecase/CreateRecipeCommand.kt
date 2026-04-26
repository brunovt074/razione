package com.recipecostcalculator.recipe.application.usecase

import com.recipecostcalculator.recipe.domain.model.Quantity

data class CreateRecipeCommand(
    val name: String,
    val yield: Quantity,
)
