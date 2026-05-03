package com.recipecostcalculator.pricing.application.dto

import com.recipecostcalculator.financial.domain.model.Money
import com.recipecostcalculator.pricing.domain.model.PricingConfiguration
import java.math.BigDecimal

data class PricingSummaryDto(
    val variableCostPerPizza: Money,
    val fixedCostPerPizza: Money,
    val totalCostPerPizza: Money,
    val pricingConfiguration: PricingConfiguration,
    val commissionAmount: Money,
    val netIncome: Money,
    val grossProfit: Money,
    val grossMarginPercent: BigDecimal,
    val markupPercent: BigDecimal,
    val suggestedPriceForTargetMargin: Money,
    val suggestedPriceWithCommission: Money,
)
