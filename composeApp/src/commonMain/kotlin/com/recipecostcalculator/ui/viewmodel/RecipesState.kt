package com.recipecostcalculator.ui.viewmodel

import com.recipecostcalculator.domain.model.CostBreakdown
import com.recipecostcalculator.domain.model.Recipe

data class RecipesState(
    val recipes: List<Recipe> = emptyList(),
    val costBreakdowns: Map<Long, CostBreakdown?> = emptyMap(),
    val isLoading: Boolean = true,
    val error: String? = null
)
