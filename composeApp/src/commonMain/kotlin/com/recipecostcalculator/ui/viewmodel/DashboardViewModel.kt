package com.recipecostcalculator.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.recipecostcalculator.domain.model.CostBreakdown
import com.recipecostcalculator.domain.repository.FixedCostRepository
import com.recipecostcalculator.domain.repository.RecipeRepository
import com.recipecostcalculator.domain.repository.SettingsRepository
import com.recipecostcalculator.domain.usecase.CalculateRecipeCostUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val recipeRepository: RecipeRepository,
    private val fixedCostRepository: FixedCostRepository,
    private val settingsRepository: SettingsRepository,
    private val calculateRecipeCostUseCase: CalculateRecipeCostUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    init {
        loadDashboard()
    }

    fun loadDashboard() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            val recipes = recipeRepository.getBaseRecipes()
            val totalFixed = fixedCostRepository.getTotalMonthly()
            val settings = settingsRepository.getSettings()

            val costs = recipes.mapNotNull { recipe ->
                calculateRecipeCostUseCase(recipe.id).getOrNull()
            }

            val avgVariable = if (costs.isNotEmpty()) costs.sumOf { it.totalVariableCost } / costs.size else 0.0
            val avgTotal = if (costs.isNotEmpty()) costs.sumOf { it.totalCostPerUnit } / costs.size else 0.0

            _state.update {
                it.copy(
                    recipesWithCosts = costs,
                    totalFixedCosts = totalFixed,
                    estimatedProduction = settings.estimatedMonthlyProduction,
                    avgVariableCost = avgVariable,
                    avgTotalCost = avgTotal,
                    isLoading = false
                )
            }
        }
    }

    fun onRefresh() {
        loadDashboard()
    }
}

data class DashboardState(
    val recipesWithCosts: List<CostBreakdown> = emptyList(),
    val totalFixedCosts: Double = 0.0,
    val estimatedProduction: Int = 0,
    val avgVariableCost: Double = 0.0,
    val avgTotalCost: Double = 0.0,
    val isLoading: Boolean = true
)