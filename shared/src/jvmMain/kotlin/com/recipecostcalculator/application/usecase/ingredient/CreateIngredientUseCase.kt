package com.recipecostcalculator.application.usecase.ingredient

import com.recipecostcalculator.domain.model.ingredient.Ingredient
import com.recipecostcalculator.domain.model.valueobject.Money
import com.recipecostcalculator.domain.model.valueobject.UnitOfMeasure
import com.recipecostcalculator.domain.repository.IngredientRepository

class CreateIngredientUseCase(
    private val ingredientRepository: IngredientRepository,
) {
    data class Command(
        val name: String,
        val unit: UnitOfMeasure,
        val costPerUnit: Money,
    )

    operator fun invoke(command: Command): Ingredient {
        val ingredient = Ingredient(
            name = command.name.trim(),
            unit = command.unit,
            costPerUnit = command.costPerUnit,
        )
        return ingredientRepository.save(ingredient)
    }
}
