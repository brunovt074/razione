package com.recipecostcalculator.domain.model.ingredient

import com.recipecostcalculator.domain.model.valueobject.Money
import com.recipecostcalculator.domain.model.valueobject.Quantity
import com.recipecostcalculator.domain.model.valueobject.UnitOfMeasure

data class Ingredient(
    val id: Long? = null,
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
