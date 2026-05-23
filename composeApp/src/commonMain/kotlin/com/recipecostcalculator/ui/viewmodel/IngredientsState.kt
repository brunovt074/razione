package com.recipecostcalculator.ui.viewmodel

import com.recipecostcalculator.domain.model.Ingredient

data class IngredientsState(
    val ingredients: List<Ingredient> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val searchQuery: String = ""
)
