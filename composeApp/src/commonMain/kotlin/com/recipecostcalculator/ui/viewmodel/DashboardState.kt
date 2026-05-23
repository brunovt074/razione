package com.recipecostcalculator.ui.viewmodel

import com.recipecostcalculator.domain.model.CostBreakdown
import com.recipecostcalculator.financial.domain.model.Money

data class DashboardState(
    val recipesWithCosts: List<CostBreakdown> = emptyList(),
    val totalFixedCosts: Money = Money.ZERO,
    val estimatedProduction: Int = 0,
    val avgVariableCost: Money = Money.ZERO,
    val avgTotalCost: Money = Money.ZERO,
    val isLoading: Boolean = true
)
