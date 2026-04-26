package com.recipecostcalculator.recipe.application.usecase

import com.recipecostcalculator.recipe.domain.model.Recipe
import com.recipecostcalculator.recipe.domain.model.SubRecipeComponent
import com.recipecostcalculator.recipe.domain.repository.RecipeRepository

class AddComponentToRecipeUseCase(
    private val recipeRepository: RecipeRepository,
) {
    fun execute(command: AddComponentToRecipeCommand): Recipe {
        val recipe = recipeRepository.findById(command.recipeId)
            ?: throw IllegalArgumentException("Receta no encontrada: ${command.recipeId}")

        if (command.component is SubRecipeComponent && command.component.recipeId == command.recipeId) {
            throw IllegalArgumentException("Una receta no puede incluirse a si misma")
        }

        val updated = recipe.withComponent(command.component)
        return recipeRepository.save(updated)
    }
}
