package com.recipecostcalculator.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.recipecostcalculator.domain.model.FixedCost
import com.recipecostcalculator.domain.repository.FixedCostRepository
import com.recipecostcalculator.financial.domain.model.Money
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CostosFijosViewModel(
    private val fixedCostRepository: FixedCostRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CostosFijosState())
    val state: StateFlow<CostosFijosState> = _state.asStateFlow()

    init {
        loadCosts()
    }

    private fun loadCosts() {
        viewModelScope.launch {
            fixedCostRepository.observeAll().collect { list ->
                val total = list.sumOf { it.monthlyAmount.amount.toDouble() }
                _state.update { it.copy(costs = list, totalMonthly = Money.of(total), isLoading = false) }
            }
        }
    }

    fun onSave(cost: FixedCost) {
        viewModelScope.launch {
            runCatching {
                if (cost.id == 0L) fixedCostRepository.insert(cost)
                else fixedCostRepository.update(cost)
            }.onFailure { e -> _state.update { it.copy(error = e.message) } }
        }
    }

    fun onDelete(id: Long) {
        viewModelScope.launch { fixedCostRepository.delete(id) }
    }

    fun onErrorShown() {
        _state.update { it.copy(error = null) }
    }
}

data class CostosFijosState(
    val costs: List<FixedCost> = emptyList(),
    val totalMonthly: Money = Money.ZERO,
    val isLoading: Boolean = true,
    val error: String? = null
)