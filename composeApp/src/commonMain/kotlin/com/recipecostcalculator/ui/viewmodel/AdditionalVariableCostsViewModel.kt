package com.recipecostcalculator.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.recipecostcalculator.domain.model.AdditionalVariableCost
import com.recipecostcalculator.domain.repository.AdditionalVariableCostRepository
import com.recipecostcalculator.financial.domain.model.Money
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AdditionalVariableCostsViewModel(
    private val additionalCostRepository: AdditionalVariableCostRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AdditionalVariableCostsState())
    val state: StateFlow<AdditionalVariableCostsState> = _state.asStateFlow()

    fun loadCosts() {
        viewModelScope.launch {
            additionalCostRepository.observeAll().collect { list ->
                val total = list.fold(Money.ZERO) { acc, cost -> acc + cost.unitCost }
                _state.update { it.copy(costs = list, totalCost = total, isLoading = false) }
            }
        }
    }

    fun onSave(cost: AdditionalVariableCost) {
        viewModelScope.launch {
            runCatching {
                if (cost.id == 0L) additionalCostRepository.insert(cost)
                else additionalCostRepository.update(cost)
            }.onFailure { e -> _state.update { it.copy(error = e.message) } }
        }
    }

    fun onDelete(id: Long) {
        viewModelScope.launch { additionalCostRepository.delete(id) }
    }

    fun onErrorShown() {
        _state.update { it.copy(error = null) }
    }
}

data class AdditionalVariableCostsState(
    val costs: List<AdditionalVariableCost> = emptyList(),
    val totalCost: Money = Money.ZERO,
    val isLoading: Boolean = true,
    val error: String? = null
)
