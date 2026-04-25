package com.recipecostcalculator.application.usecase.ingredient

import com.recipecostcalculator.domain.model.ingredient.Ingredient
import com.recipecostcalculator.domain.model.valueobject.Money
import com.recipecostcalculator.domain.repository.IngredientRepository

class UpdateIngredientCostUseCase(
    private val ingredientRepository: IngredientRepository,
) {
    data class Command(
        val ingredientId: Long,
        val costPerUnit: Money,
    )

    operator fun invoke(command: Command): Ingredient {
        val current = ingredientRepository.findById(command.ingredientId)
            ?: throw IllegalArgumentException("Ingrediente no encontrado: ${command.ingredientId}")

        val updated = current.updateCost(command.costPerUnit)
        return ingredientRepository.save(updated)
    }
}
