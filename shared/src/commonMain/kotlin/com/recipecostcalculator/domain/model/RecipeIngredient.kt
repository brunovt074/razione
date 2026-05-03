package com.recipecostcalculator.domain.model

data class RecipeIngredient(
    val id: Long = 0,
    val recipeId: Long,
    val ingredientId: Long,
    val ingredient: Ingredient? = null,
    val usagePerPizza: Double?,
    val yieldPizzas: Int?
)