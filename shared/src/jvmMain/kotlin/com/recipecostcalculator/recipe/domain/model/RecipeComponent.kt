package com.recipecostcalculator.recipe.domain.model

sealed class RecipeComponent(
    open val quantity: Quantity,
)
