package com.recipecostcalculator.fixedcost.application.usecase

import com.recipecostcalculator.financial.domain.model.Money

data class AddFixedCostItemCommand(
    val name: String,
    val monthlyAmount: Money,
)
