package com.recipecostcalculator.ingredient.domain.model

import com.recipecostcalculator.financial.domain.model.Money
import com.recipecostcalculator.financial.domain.model.Quantity
import com.recipecostcalculator.measurement.MeasurementDimension
import com.recipecostcalculator.measurement.MeasurementUnit

data class Ingredient(
    val id: Long = 0,
    val name: String,
    val dimension: MeasurementDimension = MeasurementDimension.MASS,
    val purchaseUnit: MeasurementUnit = MeasurementUnit.KG,
    val purchasePackageLabel: String? = null,
    val purchasePrice: Money,
    val contentAmount: Quantity,
    val usageUnit: MeasurementUnit = MeasurementUnit.KG,
    val isActive: Boolean = true,
    val updatedAt: Long = 0,
) {
    init {
        require(name.isNotBlank()) { "Ingredient name cannot be blank" }
    }
}
