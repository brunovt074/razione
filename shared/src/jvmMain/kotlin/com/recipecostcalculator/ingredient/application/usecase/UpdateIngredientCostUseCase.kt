package com.recipecostcalculator.ingredient.application.usecase

import com.recipecostcalculator.ingredient.domain.model.Ingredient
import com.recipecostcalculator.ingredient.domain.repository.IngredientRepository

class UpdateIngredientCostUseCase(
    private val ingredientRepository: IngredientRepository,
) {
    fun execute(command: UpdateIngredientCostCommand): Ingredient {
        val current = ingredientRepository.findById(command.ingredientId)
            ?: throw IllegalArgumentException("Ingrediente no encontrado: ${command.ingredientId}")

        val updated = current.updateCost(command.costPerUnit)
        return ingredientRepository.save(updated)
    }
}
