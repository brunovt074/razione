package com.recipecostcalculator.domain.model

data class FixedCost(
    val id: Long = 0,
    val concept: String,
    val monthlyAmount: Double
)