package com.recipecostcalculator.recipe.application.usecase

import com.recipecostcalculator.recipe.domain.model.Recipe
import com.recipecostcalculator.recipe.domain.repository.RecipeRepository
import kotlinx.coroutines.flow.Flow

class GetAllRecipesUseCase(
    private val repository: RecipeRepository,
) {
    fun execute(): Flow<List<Recipe>> = repository.observeAll()
}