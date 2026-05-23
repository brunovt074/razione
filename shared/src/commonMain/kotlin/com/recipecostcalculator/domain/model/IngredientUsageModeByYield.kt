package com.recipecostcalculator.domain.model

data class ByYield(val pizzasPerPurchaseUnit: Int) : IngredientUsageMode() {
    init { require(pizzasPerPurchaseUnit > 0) { "Yield must be positive" } }
}
