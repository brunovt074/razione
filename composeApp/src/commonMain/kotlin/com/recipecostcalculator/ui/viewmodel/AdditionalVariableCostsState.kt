package com.recipecostcalculator.ui.viewmodel

import com.recipecostcalculator.domain.model.AdditionalVariableCost
import com.recipecostcalculator.financial.domain.model.Money

data class AdditionalVariableCostsState(
    val costs: List<AdditionalVariableCost> = emptyList(),
    val totalCost: Money = Money.ZERO,
    val isLoading: Boolean = true,
    val error: String? = null
)
