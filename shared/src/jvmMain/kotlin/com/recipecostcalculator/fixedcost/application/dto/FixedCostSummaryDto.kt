package com.recipecostcalculator.fixedcost.application.dto

import com.recipecostcalculator.financial.domain.model.Money
import java.math.BigDecimal

data class FixedCostSummaryDto(
    val totalMonthlyFixedCost: Money,
    val productionPerMonth: BigDecimal,
    val fixedCostPerPizza: Money,
)
