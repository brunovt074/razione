package com.recipecostcalculator.domain.model

data class Ingredient(
    val id: Long = 0,
    val name: String,
    val purchaseUnit: String,
    val purchasePrice: Double,
    val contentAmount: Double,
    val usageUnit: String,
    val isActive: Boolean = true,
    val updatedAt: Long
)