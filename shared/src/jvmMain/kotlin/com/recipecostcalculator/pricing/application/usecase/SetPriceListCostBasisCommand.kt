package com.recipecostcalculator.pricing.application.usecase

import com.recipecostcalculator.pricing.domain.model.CostBasisType

data class SetPriceListCostBasisCommand(
    val costBasisType: CostBasisType,
)
