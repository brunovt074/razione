package com.recipecostcalculator.financial.domain.model

import com.recipecostcalculator.measurement.MeasurementUnit
import com.recipecostcalculator.measurement.UnitConverter

data class Quantity(val value: Double, val unit: MeasurementUnit) {
    init {
        require(value >= 0.0) { "Quantity must be non-negative, got $value" }
    }

    fun toCanonical(): Quantity {
        val canonical = MeasurementUnit.canonicalFor(unit.dimension)
        return Quantity(UnitConverter.toCanonical(value, unit), canonical)
    }

    fun convertTo(target: MeasurementUnit): Quantity =
        Quantity(UnitConverter.convert(value, unit, target), target)

    companion object {
        fun of(value: Double, unit: MeasurementUnit): Quantity = Quantity(value, unit)
    }
}
