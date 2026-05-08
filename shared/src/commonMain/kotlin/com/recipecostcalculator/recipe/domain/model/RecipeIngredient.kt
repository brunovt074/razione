package com.recipecostcalculator.recipe.domain.model

import com.recipecostcalculator.ingredient.domain.model.IngredientUsageMode

data class RecipeIngredient(
    val id: Long = 0,
    val ingredientId: Long,
    val primaryMode: IngredientUsageMode,
)