package com.recipecostcalculator.domain.model

import com.recipecostcalculator.financial.domain.model.Money
import com.recipecostcalculator.financial.domain.model.Quantity
import com.recipecostcalculator.measurement.MeasurementDimension
import com.recipecostcalculator.measurement.MeasurementUnit

data class Ingredient(
    val id: Long = 0,
    val name: String,
    val dimension: MeasurementDimension,
    val purchaseUnit: MeasurementUnit,
    val purchasePackageLabel: String? = null,
    val purchasePrice: Money,
    val contentAmount: Quantity,
    val usageUnit: MeasurementUnit,
    val isActive: Boolean = true,
    val updatedAt: Long = 0,
) {
    init {
        require(name.isNotBlank()) { "Ingredient name cannot be blank" }
        require(purchaseUnit.dimension == dimension) { "purchaseUnit must match dimension" }
        require(contentAmount.unit.dimension == dimension) { "contentAmount unit must match dimension" }
        require(usageUnit.dimension == dimension) { "usageUnit must match dimension" }
    }

    fun unitCost(): Money {
        val contentInUsageUnit = contentAmount.convertTo(usageUnit).value
        if (contentInUsageUnit == 0.0) return Money.ZERO
        return purchasePrice / contentInUsageUnit
    }

    fun pricePerUnit(): Money = unitCost()
}
