package com.recipecostcalculator.application.usecase.recipe

import com.recipecostcalculator.domain.model.recipe.Recipe
import com.recipecostcalculator.domain.model.valueobject.Quantity
import com.recipecostcalculator.domain.repository.RecipeRepository

class CreateRecipeUseCase(
    private val recipeRepository: RecipeRepository,
) {
    data class Command(
        val name: String,
        val yield: Quantity,
    )

    operator fun invoke(command: Command): Recipe {
        val recipe = Recipe(
            name = command.name.trim(),
            yield = command.yield,
        )
        return recipeRepository.save(recipe)
    }
}
