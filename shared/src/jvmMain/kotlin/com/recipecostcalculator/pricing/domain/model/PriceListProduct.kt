package com.recipecostcalculator.pricing.domain.model

import com.recipecostcalculator.financial.domain.model.Money

data class PriceListProduct(
    val id: Long = 0,
    val name: String,
    val sellingPrice: Money,
    val notes: String,
) {
    init {
        require(name.isNotBlank()) { "El nombre del producto es obligatorio" }
    }
}
