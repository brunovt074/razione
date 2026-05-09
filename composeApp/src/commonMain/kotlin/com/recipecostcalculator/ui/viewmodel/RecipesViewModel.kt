package com.recipecostcalculator.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.recipecostcalculator.domain.model.CostBreakdown
import com.recipecostcalculator.domain.model.Recipe
import com.recipecostcalculator.domain.repository.AdditionalVariableCostRepository
import com.recipecostcalculator.domain.repository.IngredientRepository
import com.recipecostcalculator.domain.repository.RecipeRepository
import com.recipecostcalculator.domain.usecase.CalculateRecipeCostUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RecipesViewModel(
    private val recipeRepository: RecipeRepository,
    private val ingredientRepository: IngredientRepository,
    private val additionalCostRepository: AdditionalVariableCostRepository,
    private val calculateRecipeCostUseCase: CalculateRecipeCostUseCase
) : ViewModel() {

    private val _error = MutableStateFlow<String?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val _data: StateFlow<RecipesState> = combine(
        recipeRepository.observeAll(),
        ingredientRepository.observeAll(),
        additionalCostRepository.observeAll()
    ) { _, _, _ -> Unit }
    .flatMapLatest {
        flow {
            val recipes = recipeRepository.getAll()
            val costsMap = recipes.associate { recipe ->
                recipe.id to calculateRecipeCostUseCase(recipe.id).getOrNull()
            }
            emit(RecipesState(recipes = recipes, costBreakdowns = costsMap, isLoading = false))
        }
    }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RecipesState())

    val state: StateFlow<RecipesState> = combine(_data, _error) { data, error ->
        data.copy(error = error)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RecipesState())

    fun loadRecipes() = Unit

    fun onSave(recipe: Recipe) {
        viewModelScope.launch {
            runCatching {
                if (recipe.id == 0L) recipeRepository.insert(recipe)
                else recipeRepository.update(recipe)
            }.onFailure { e -> _error.update { e.message } }
        }
    }

    fun onDelete(id: Long) {
        viewModelScope.launch { recipeRepository.delete(id) }
    }

    fun onErrorShown() {
        _error.value = null
    }
}

data class RecipesState(
    val recipes: List<Recipe> = emptyList(),
    val costBreakdowns: Map<Long, CostBreakdown?> = emptyMap(),
    val isLoading: Boolean = true,
    val error: String? = null
)
