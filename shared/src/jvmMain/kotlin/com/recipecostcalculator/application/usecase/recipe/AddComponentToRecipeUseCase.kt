package com.recipecostcalculator.application.usecase.recipe

import com.recipecostcalculator.domain.model.recipe.Recipe
import com.recipecostcalculator.domain.model.recipe.RecipeComponent
import com.recipecostcalculator.domain.model.recipe.SubRecipeComponent
import com.recipecostcalculator.domain.repository.RecipeRepository

class AddComponentToRecipeUseCase(
    private val recipeRepository: RecipeRepository,
) {
    data class Command(
        val recipeId: Long,
        val component: RecipeComponent,
    )

    operator fun invoke(command: Command): Recipe {
        val recipe = recipeRepository.findById(command.recipeId)
            ?: throw IllegalArgumentException("Receta no encontrada: ${command.recipeId}")

        if (command.component is SubRecipeComponent && command.component.recipeId == command.recipeId) {
            throw IllegalArgumentException("Una receta no puede incluirse a si misma")
        }

        val updated = recipe.withComponent(command.component)
        return recipeRepository.save(updated)
    }
}
