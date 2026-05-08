package com.recipecostcalculator.pricing.domain.model

import com.recipecostcalculator.financial.domain.model.Money

data class SuggestedPrice(
    val withoutCommission: Money,
    val withCommission: Money,
)