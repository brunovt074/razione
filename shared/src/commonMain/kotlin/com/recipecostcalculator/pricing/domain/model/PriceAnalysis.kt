package com.recipecostcalculator.pricing.domain.model

import com.recipecostcalculator.financial.domain.model.Money
import com.recipecostcalculator.financial.domain.model.Percentage

data class PriceAnalysis(
    val recipeId: Long,
    val recipeName: String,
    val salePrice: Money,
    val commissionAmount: Money,
    val netIncome: Money,
    val variableCost: Money,
    val grossProfit: Money,
    val grossMargin: Percentage?,
    val markup: Double?,
    val suggestedPrice: SuggestedPrice,
    val costMode: CostMode,
    val chosenCost: Money,
)