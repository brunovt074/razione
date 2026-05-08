package com.recipecostcalculator.ingredient.domain.model

import com.recipecostcalculator.financial.domain.model.Money
import com.recipecostcalculator.financial.domain.model.Quantity

data class Ingredient(
    val id: Long = 0,
    val name: String,
    val purchaseUnit: String,
    val purchasePrice: Money,
    val contentAmount: Quantity,
    val usageUnit: String,
    val isActive: Boolean = true,
    val updatedAt: Long = 0,
) {
    init {
        require(name.isNotBlank()) { "Ingredient name cannot be blank" }
        require(purchaseUnit.isNotBlank()) { "Purchase unit cannot be blank" }
        require(usageUnit.isNotBlank()) { "Usage unit cannot be blank" }
    }
}