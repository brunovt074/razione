package com.recipecostcalculator.domain.model

data class RecipeIngredient(
    val id: Long = 0,
    val recipeId: Long = 0,
    val ingredientId: Long,
    val primaryMode: IngredientUsageMode,
    val ingredient: Ingredient? = null
)