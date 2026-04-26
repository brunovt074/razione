package com.recipecostcalculator.ingredient.domain.model

import com.recipecostcalculator.financial.domain.model.Money
import com.recipecostcalculator.recipe.domain.model.Quantity
import com.recipecostcalculator.recipe.domain.model.UnitOfMeasure

data class Ingredient(
    val id: Long = 0,
    val name: String,
    val unit: UnitOfMeasure,
    val costPerUnit: Money,
) {
    init {
        require(name.isNotBlank()) { "El nombre del ingrediente es obligatorio" }
    }

    fun costFor(quantity: Quantity): Money {
        val normalizedQuantity = quantity.to(unit)
        return costPerUnit * normalizedQuantity.value
    }

    fun updateCost(newCostPerUnit: Money): Ingredient = copy(costPerUnit = newCostPerUnit)
}
