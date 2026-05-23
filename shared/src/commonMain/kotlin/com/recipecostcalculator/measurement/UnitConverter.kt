package com.recipecostcalculator.measurement

object UnitConverter {
    fun toCanonical(amount: Double, unit: MeasurementUnit): Double =
        amount * unit.toCanonicalFactor

    fun convert(amount: Double, from: MeasurementUnit, to: MeasurementUnit): Double {
        require(from.dimension == to.dimension) {
            "Cannot convert ${from.name} (${from.dimension}) to ${to.name} (${to.dimension})"
        }
        return amount * from.toCanonicalFactor / to.toCanonicalFactor
    }
}
