package com.recipecostcalculator.domain.model

import com.recipecostcalculator.financial.domain.model.Quantity

sealed class IngredientUsageMode {
    data class ByUsage(val amountPerPizza: Quantity) : IngredientUsageMode()
    data class ByYield(val pizzasPerPurchaseUnit: Int) : IngredientUsageMode() {
        init { require(pizzasPerPurchaseUnit > 0) { "Yield must be positive" } }
    }
}