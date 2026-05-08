package com.recipecostcalculator.ingredient.application.usecase

import com.recipecostcalculator.ingredient.domain.model.Ingredient
import com.recipecostcalculator.ingredient.domain.model.IngredientUsageMode
import com.recipecostcalculator.ingredient.domain.repository.IngredientRepository
import com.recipecostcalculator.util.currentTimeMillis

class CreateIngredientUseCase(
    private val repository: IngredientRepository,
) {
    data class Command(
        val name: String,
        val purchaseUnit: String,
        val purchasePrice: Double,
        val contentAmount: Double,
        val usageUnit: String,
    )

    suspend fun execute(command: Command): Result<Ingredient> = runCatching {
        require(command.name.isNotBlank()) { "El nombre no puede estar vacío" }
        require(command.purchaseUnit.isNotBlank()) { "La unidad de compra no puede estar vacía" }
        require(command.usageUnit.isNotBlank()) { "La unidad de uso no puede estar vacía" }
        require(command.purchasePrice > 0.0) { "El precio debe ser mayor a cero" }
        require(command.contentAmount > 0.0) { "El contenido debe ser mayor a cero" }

        val ingredient = Ingredient(
            name = command.name.trim(),
            purchaseUnit = command.purchaseUnit,
            purchasePrice = com.recipecostcalculator.financial.domain.model.Money.of(command.purchasePrice),
            contentAmount = com.recipecostcalculator.financial.domain.model.Quantity.of(command.contentAmount),
            usageUnit = command.usageUnit,
            updatedAt = currentTimeMillis(),
        )
        repository.save(ingredient)
    }
}