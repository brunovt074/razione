package com.recipecostcalculator.recipe.application.usecase

import com.recipecostcalculator.recipe.domain.model.Recipe
import com.recipecostcalculator.recipe.domain.repository.RecipeRepository

class GetAllRecipesUseCase(
    private val recipeRepository: RecipeRepository,
) {
    fun execute(): List<Recipe> = recipeRepository.findAll()
}
