package com.recipecostcalculator.domain.model.recipe

import com.recipecostcalculator.domain.model.valueobject.Quantity

sealed class RecipeComponent(
    open val quantity: Quantity,
)

data class IngredientComponent(
    val ingredientId: Long,
    override val quantity: Quantity,
) : RecipeComponent(quantity) {
    init {
        require(ingredientId > 0) { "ingredientId invalido" }
    }
}

data class SubRecipeComponent(
    val recipeId: Long,
    override val quantity: Quantity,
) : RecipeComponent(quantity) {
    init {
        require(recipeId > 0) { "recipeId invalido" }
    }
}
