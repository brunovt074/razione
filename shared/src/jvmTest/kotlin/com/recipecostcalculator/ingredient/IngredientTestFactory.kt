package com.recipecostcalculator.ingredient

import com.recipecostcalculator.financial.domain.model.Money
import com.recipecostcalculator.ingredient.domain.model.Ingredient
import com.recipecostcalculator.recipe.domain.model.UnitOfMeasure

object IngredientTestFactory {
    fun create(
        id: Long = 0,
        name: String = "Harina",
        unit: UnitOfMeasure = UnitOfMeasure.GRAM,
        costPerUnit: Money = Money.of("0.010"),
    ): Ingredient {
        return Ingredient(
            id = id,
            name = name,
            unit = unit,
            costPerUnit = costPerUnit,
        )
    }
}
