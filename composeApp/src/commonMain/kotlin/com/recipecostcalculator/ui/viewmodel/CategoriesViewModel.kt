package com.recipecostcalculator.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.recipecostcalculator.domain.model.Category
import com.recipecostcalculator.domain.repository.CategoryRepository
import com.recipecostcalculator.domain.repository.RecipeRepository
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

class CategoriesViewModel(
    private val categoryRepository: CategoryRepository,
    private val recipeRepository: RecipeRepository
) : ViewModel() {

    private val _error = MutableStateFlow<String?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val _data: StateFlow<CategoriesState> = combine(
        categoryRepository.observeAll(),
        recipeRepository.observeAll()
    ) { _, _ -> Unit }
        .flatMapLatest {
            flow {
                val categories = categoryRepository.getAllWithCount()
                emit(CategoriesState(categories = categories, isLoading = false))
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CategoriesState())

    val state: StateFlow<CategoriesState> = combine(_data, _error) { data, error ->
        data.copy(error = error)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CategoriesState())

    fun onSave(category: Category) {
        viewModelScope.launch {
            runCatching {
                if (category.id == 0L) {
                    categoryRepository.insert(category)
                } else {
                    categoryRepository.update(category)
                }
            }.onFailure { e -> _error.update { e.message } }
        }
    }

    fun onDelete(id: Long) {
        viewModelScope.launch {
            runCatching {
                val count = categoryRepository.countRecipes(id)
                if (count > 0) {
                    _error.update { "no_delete_has_recipes" }
                } else {
                    categoryRepository.delete(id)
                }
            }.onFailure { e -> _error.update { e.message } }
        }
    }

    fun onErrorShown() {
        _error.value = null
    }
}
