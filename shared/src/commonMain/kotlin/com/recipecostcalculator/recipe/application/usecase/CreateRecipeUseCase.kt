package com.recipecostcalculator.recipe.application.usecase

import com.recipecostcalculator.recipe.domain.model.Recipe
import com.recipecostcalculator.recipe.domain.model.RecipeIngredient
import com.recipecostcalculator.recipe.domain.repository.RecipeRepository
import com.recipecostcalculator.util.currentTimeMillis

class CreateRecipeUseCase(
    private val repository: RecipeRepository,
) {
    data class Command(
        val name: String,
        val parentRecipeId: Long?,
        val ingredients: List<RecipeIngredient>,
    )

    suspend fun execute(command: Command): Result<Recipe> = runCatching {
        require(command.name.isNotBlank()) { "El nombre no puede estar vacío" }

        val recipe = Recipe(
            name = command.name.trim(),
            parentRecipeId = command.parentRecipeId,
            ingredients = command.ingredients,
            createdAt = currentTimeMillis(),
            updatedAt = currentTimeMillis(),
        )
        repository.save(recipe)
    }
}