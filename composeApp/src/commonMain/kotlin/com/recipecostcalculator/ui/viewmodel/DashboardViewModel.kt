package com.recipecostcalculator.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.recipecostcalculator.domain.model.CostBreakdown
import com.recipecostcalculator.domain.repository.AdditionalVariableCostRepository
import com.recipecostcalculator.domain.repository.FixedCostRepository
import com.recipecostcalculator.domain.repository.IngredientRepository
import com.recipecostcalculator.domain.repository.RecipeRepository
import com.recipecostcalculator.domain.repository.SettingsRepository
import com.recipecostcalculator.domain.usecase.CalculateRecipeCostUseCase
import com.recipecostcalculator.financial.domain.model.Money
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn

class DashboardViewModel(
    private val recipeRepository: RecipeRepository,
    private val ingredientRepository: IngredientRepository,
    private val fixedCostRepository: FixedCostRepository,
    private val additionalCostRepository: AdditionalVariableCostRepository,
    private val settingsRepository: SettingsRepository,
    private val calculateRecipeCostUseCase: CalculateRecipeCostUseCase
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<DashboardState> = combine(
        ingredientRepository.observeAll(),
        fixedCostRepository.observeAll(),
        settingsRepository.observeSettings(),
        recipeRepository.observeAll(),
        additionalCostRepository.observeAll()
    ) { _, _, _, _, _ -> Unit }
    .flatMapLatest {
        flow {
            val recipes = recipeRepository.getAllForDashboard()
            val totalFixed = fixedCostRepository.getTotalMonthly()
            val settings = settingsRepository.getSettings()
            val costs = recipes.mapNotNull { recipe ->
                calculateRecipeCostUseCase(recipe.id).getOrNull()
            }
            val totalVar = costs.sumOf { it.totalVariableCost.amount }
            val totalTot = costs.sumOf { it.totalCostPerUnit.amount }
            val avgVariable = if (costs.isNotEmpty()) Money.of(totalVar / costs.size) else Money.ZERO
            val avgTotal = if (costs.isNotEmpty()) Money.of(totalTot / costs.size) else Money.ZERO
            emit(DashboardState(
                recipesWithCosts = costs,
                totalFixedCosts = totalFixed,
                estimatedProduction = settings.estimatedMonthlyProduction,
                avgVariableCost = avgVariable,
                avgTotalCost = avgTotal,
                isLoading = false
            ))
        }
    }
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DashboardState(isLoading = true)
    )

    fun loadDashboard() = Unit
    fun onRefresh() = Unit
}

data class DashboardState(
    val recipesWithCosts: List<CostBreakdown> = emptyList(),
    val totalFixedCosts: Money = Money.ZERO,
    val estimatedProduction: Int = 0,
    val avgVariableCost: Money = Money.ZERO,
    val avgTotalCost: Money = Money.ZERO,
    val isLoading: Boolean = true
)
