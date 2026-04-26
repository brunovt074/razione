package com.recipecostcalculator.recipe.domain.model

data class IngredientComponent(
    val ingredientId: Long,
    override val quantity: Quantity,
) : RecipeComponent(quantity) {
    init {
        require(ingredientId > 0) { "ingredientId invalido" }
    }
}
