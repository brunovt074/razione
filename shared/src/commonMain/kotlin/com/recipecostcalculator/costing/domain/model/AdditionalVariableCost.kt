package com.recipecostcalculator.costing.domain.model

import com.recipecostcalculator.financial.domain.model.Money

data class AdditionalVariableCost(
    val id: Long = 0,
    val recipeId: Long?,
    val concept: String,
    val unitCost: Money,
    val note: String = "",
) {
    init {
        require(concept.isNotBlank()) { "Concept cannot be blank" }
    }
}