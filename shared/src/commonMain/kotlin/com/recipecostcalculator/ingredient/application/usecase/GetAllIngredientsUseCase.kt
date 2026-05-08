package com.recipecostcalculator.ingredient.application.usecase

import com.recipecostcalculator.ingredient.domain.model.Ingredient
import com.recipecostcalculator.ingredient.domain.repository.IngredientRepository
import kotlinx.coroutines.flow.Flow

class GetAllIngredientsUseCase(
    private val repository: IngredientRepository,
) {
    fun execute(): Flow<List<Ingredient>> = repository.observeAll()
}