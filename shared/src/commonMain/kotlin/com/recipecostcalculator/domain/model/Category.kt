package com.recipecostcalculator.domain.model

data class Category(
    val id: Long = 0,
    val name: String,
    val unitLabel: String,
    val description: String? = null,
    val sortOrder: Int = 0,
    val recipeCount: Int = 0,
    val createdAt: Long,
    val updatedAt: Long
)
