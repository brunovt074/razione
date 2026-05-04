package com.recipecostcalculator.domain.model

import com.recipecostcalculator.financial.domain.model.Money

data class FixedCost(
    val id: Long = 0,
    val concept: String,
    val monthlyAmount: Money
) {
    init {
        require(concept.isNotBlank()) { "Concept cannot be blank" }
    }
}