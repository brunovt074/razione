package com.recipecostcalculator.recipe.application.usecase

import com.recipecostcalculator.recipe.domain.model.Recipe
import com.recipecostcalculator.recipe.domain.repository.RecipeRepository

class CreateRecipeUseCase(
    private val recipeRepository: RecipeRepository,
) {
    fun execute(command: CreateRecipeCommand): Recipe {
        val recipe = Recipe(
            name = command.name.trim(),
            yield = command.yield,
        )
        return recipeRepository.save(recipe)
    }
}
