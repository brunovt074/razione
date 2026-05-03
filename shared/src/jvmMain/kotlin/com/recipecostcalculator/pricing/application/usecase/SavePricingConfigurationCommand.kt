package com.recipecostcalculator.pricing.application.usecase

import com.recipecostcalculator.financial.domain.model.Money
import java.math.BigDecimal

data class SavePricingConfigurationCommand(
    val sellingPricePerPizza: Money,
    val commissionPercent: BigDecimal,
    val wastePercent: BigDecimal,
    val targetMarginPercent: BigDecimal,
)
