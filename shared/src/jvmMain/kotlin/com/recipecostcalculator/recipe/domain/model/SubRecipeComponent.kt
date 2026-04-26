package com.recipecostcalculator.recipe.domain.model

data class SubRecipeComponent(
    val recipeId: Long,
    override val quantity: Quantity,
) : RecipeComponent(quantity) {
    init {
        require(recipeId > 0) { "recipeId invalido" }
    }
}
