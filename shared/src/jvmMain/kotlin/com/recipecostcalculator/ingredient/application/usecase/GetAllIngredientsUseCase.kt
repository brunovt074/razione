package com.recipecostcalculator.ingredient.application.usecase

import com.recipecostcalculator.ingredient.domain.model.Ingredient
import com.recipecostcalculator.ingredient.domain.repository.IngredientRepository

class GetAllIngredientsUseCase(
    private val ingredientRepository: IngredientRepository,
) {
    fun execute(): List<Ingredient> = ingredientRepository.findAll()
}
