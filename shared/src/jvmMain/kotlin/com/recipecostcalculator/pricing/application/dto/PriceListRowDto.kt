package com.recipecostcalculator.pricing.application.dto

data class PriceListRowDto(
    val productId: Long,
    val productName: String,
    val sellingPrice: String,
    val costUsed: String,
    val grossProfit: String,
    val marginPercent: String,
    val notes: String,
)
