package com.recipecostcalculator.ingredient.application.command

data class IngredientCommand(
    val id: Long? = null,
    val name: String,
    val purchaseUnit: String,
    val purchasePrice: Double,
    val contentAmount: Double,
    val usageUnit: String,
)