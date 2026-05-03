package com.recipecostcalculator.domain.model

data class AdditionalVariableCost(
    val id: Long = 0,
    val recipeId: Long?,
    val concept: String,
    val unitCost: Double,
    val note: String = ""
)