package com.recipecostcalculator.ingredient.application.usecase

import com.recipecostcalculator.financial.domain.model.Money
import com.recipecostcalculator.financial.domain.model.Quantity
import com.recipecostcalculator.ingredient.application.command.IngredientCommand
import com.recipecostcalculator.ingredient.domain.model.Ingredient
import com.recipecostcalculator.ingredient.domain.repository.IngredientRepository
import com.recipecostcalculator.measurement.MeasurementUnit
import com.recipecostcalculator.util.currentTimeMillis

class CreateIngredientUseCase(
    private val repository: IngredientRepository,
) {
    suspend fun execute(command: IngredientCommand): Result<Ingredient> = runCatching {
        require(command.name.isNotBlank()) { "El nombre no puede estar vacío" }
        require(command.purchaseUnit.isNotBlank()) { "La unidad de compra no puede estar vacía" }
        require(command.usageUnit.isNotBlank()) { "La unidad de uso no puede estar vacía" }
        require(command.purchasePrice > 0.0) { "El precio debe ser mayor a cero" }
        require(command.contentAmount > 0.0) { "El contenido debe ser mayor a cero" }

        val purchaseUnit = MeasurementUnit.values().firstOrNull { it.name == command.purchaseUnit } ?: MeasurementUnit.KG
        val usageUnit = MeasurementUnit.values().firstOrNull { it.name == command.usageUnit } ?: purchaseUnit

        val ingredient = Ingredient(
            name = command.name.trim(),
            dimension = purchaseUnit.dimension,
            purchaseUnit = purchaseUnit,
            purchasePrice = Money.of(command.purchasePrice),
            contentAmount = Quantity(command.contentAmount, purchaseUnit),
            usageUnit = usageUnit,
            updatedAt = currentTimeMillis(),
        )
        repository.save(ingredient)
    }
}
