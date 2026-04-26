package com.recipecostcalculator.ingredient.application.usecase

import com.recipecostcalculator.ingredient.domain.model.Ingredient
import com.recipecostcalculator.ingredient.domain.repository.IngredientRepository

class CreateIngredientUseCase(
    private val ingredientRepository: IngredientRepository,
) {
    fun execute(command: CreateIngredientCommand): Ingredient {
        val ingredient = Ingredient(
            name = command.name.trim(),
            unit = command.unit,
            costPerUnit = command.costPerUnit,
        )
        return ingredientRepository.save(ingredient)
    }
}
