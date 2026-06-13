package com.recipecostcalculator.domain.model

data class Recipe(
    val id: Long = 0,
    val name: String,
    val parentRecipeId: Long?,
    val categoryId: Long? = null,
    val recipeIngredients: List<RecipeIngredient> = emptyList(),
    val createdAt: Long,
    val updatedAt: Long
)