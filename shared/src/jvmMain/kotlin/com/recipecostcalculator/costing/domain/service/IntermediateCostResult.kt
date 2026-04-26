package com.recipecostcalculator.costing.domain.service

import com.recipecostcalculator.financial.domain.model.Money

data class IntermediateCostResult(
    val totalCost: Money,
    val breakdown: LinkedHashMap<Long, IngredientBreakdownAccumulator>,
)
