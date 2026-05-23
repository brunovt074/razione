package com.recipecostcalculator.ui.viewmodel

import com.recipecostcalculator.domain.model.FixedCost
import com.recipecostcalculator.financial.domain.model.Money

data class FixedCostsState(
    val costs: List<FixedCost> = emptyList(),
    val totalMonthly: Money = Money.ZERO,
    val isLoading: Boolean = true,
    val error: String? = null
)
