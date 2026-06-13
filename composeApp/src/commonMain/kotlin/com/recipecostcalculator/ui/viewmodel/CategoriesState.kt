package com.recipecostcalculator.ui.viewmodel

import com.recipecostcalculator.domain.model.Category

data class CategoriesState(
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)
