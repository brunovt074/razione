package com.recipecostcalculator.recipe.domain.model

data class Recipe(
    val id: Long = 0,
    val name: String,
    val parentRecipeId: Long? = null,
    val ingredients: List<RecipeIngredient> = emptyList(),
    val createdAt: Long = 0,
    val updatedAt: Long = 0,
) {
    init {
        require(name.isNotBlank()) { "Recipe name cannot be blank" }
        require(parentRecipeId != id || id == 0L) {
            "A recipe cannot be its own parent"
        }
    }

    fun isBaseRecipe(): Boolean = parentRecipeId == null
}