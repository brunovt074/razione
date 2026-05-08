package com.recipecostcalculator.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.recipecostcalculator.domain.model.Ingredient
import com.recipecostcalculator.domain.repository.IngredientRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class IngredientsViewModel(
    private val ingredientRepository: IngredientRepository
) : ViewModel() {

    private val _state = MutableStateFlow(IngredientsState())
    val state: StateFlow<IngredientsState> = _state.asStateFlow()

    fun loadIngredients() {
        viewModelScope.launch {
            ingredientRepository.observeAll().collect { list ->
                _state.update { it.copy(ingredients = list, isLoading = false) }
            }
        }
    }

    fun onUpdatePrice(id: Long, newPrice: Double) {
        viewModelScope.launch {
            runCatching { ingredientRepository.updatePrice(id, newPrice) }
                .onFailure { e -> _state.update { it.copy(error = e.message) } }
        }
    }

    fun onSave(ingredient: Ingredient) {
        viewModelScope.launch {
            runCatching {
                if (ingredient.id == 0L) ingredientRepository.insert(ingredient)
                else ingredientRepository.update(ingredient)
            }.onFailure { e -> _state.update { it.copy(error = e.message) } }
        }
    }

    fun onDelete(id: Long) {
        viewModelScope.launch { ingredientRepository.delete(id) }
    }

    fun onErrorShown() {
        _state.update { it.copy(error = null) }
    }

    fun onSearchQueryChanged(query: String) {
        _state.update { it.copy(searchQuery = query) }
    }
}

data class IngredientsState(
    val ingredients: List<Ingredient> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val searchQuery: String = ""
)