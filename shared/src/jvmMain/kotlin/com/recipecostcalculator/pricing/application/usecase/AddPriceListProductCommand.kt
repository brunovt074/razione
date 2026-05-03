package com.recipecostcalculator.pricing.application.usecase

import com.recipecostcalculator.financial.domain.model.Money

data class AddPriceListProductCommand(
    val name: String,
    val sellingPrice: Money,
    val notes: String,
)
