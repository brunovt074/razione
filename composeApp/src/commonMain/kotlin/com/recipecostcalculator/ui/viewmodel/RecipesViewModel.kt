package com.recipecostcalculator.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.recipecostcalculator.domain.model.CostBreakdown
import com.recipecostcalculator.domain.model.Recipe
import com.recipecostcalculator.domain.repository.RecipeRepository
import com.recipecostcalculator.domain.usecase.CalculateRecipeCostUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RecipesViewModel(
    private val recipeRepository: RecipeRepository,
    private val calculateRecipeCostUseCase: CalculateRecipeCostUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(RecipesState())
    val state: StateFlow<RecipesState> = _state.asStateFlow()

    fun loadRecipes() {
        viewModelScope.launch {
            recipeRepository.observeAll().collect { list ->
                _state.update { it.copy(recipes = list, isLoading = false) }
                loadAllCosts()
            }
        }
    }

    private fun loadAllCosts() {
        viewModelScope.launch {
            val recipes = _state.value.recipes
            val costsMap = mutableMapOf<Long, CostBreakdown?>()
            recipes.forEach { recipe ->
                val result = calculateRecipeCostUseCase(recipe.id)
                costsMap[recipe.id] = result.getOrNull()
            }
            _state.update { it.copy(costBreakdowns = costsMap) }
        }
    }

    fun onSave(recipe: Recipe) {
        viewModelScope.launch {
            runCatching {
                if (recipe.id == 0L) recipeRepository.insert(recipe)
                else recipeRepository.update(recipe)
            }.onFailure { e -> _state.update { it.copy(error = e.message) } }
        }
    }

    fun onDelete(id: Long) {
        viewModelScope.launch { recipeRepository.delete(id) }
    }

    fun onErrorShown() {
        _state.update { it.copy(error = null) }
    }
}

data class RecipesState(
    val recipes: List<Recipe> = emptyList(),
    val costBreakdowns: Map<Long, CostBreakdown?> = emptyMap(),
    val isLoading: Boolean = true,
    val error: String? = null
)