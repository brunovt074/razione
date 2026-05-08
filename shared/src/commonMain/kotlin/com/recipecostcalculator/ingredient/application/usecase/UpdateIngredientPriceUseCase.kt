package com.recipecostcalculator.ingredient.application.usecase

import com.recipecostcalculator.ingredient.domain.model.Ingredient
import com.recipecostcalculator.ingredient.domain.repository.IngredientRepository
import com.recipecostcalculator.util.currentTimeMillis
import com.recipecostcalculator.financial.domain.model.Money

class UpdateIngredientPriceUseCase(
    private val repository: IngredientRepository,
) {
    data class Command(val ingredientId: Long, val newPrice: Double)

    suspend fun execute(command: Command): Result<Ingredient> = runCatching {
        require(command.newPrice > 0.0) { "El precio debe ser mayor a cero" }
        repository.updatePrice(
            id = command.ingredientId,
            newPrice = Money.of(command.newPrice),
            updatedAt = currentTimeMillis(),
        )
    }
}