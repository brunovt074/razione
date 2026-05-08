package com.recipecostcalculator.domain.service

object UnitConversionService {
    private val factors = mapOf(
        "kg" to mapOf("kg" to 1.0, "g" to 1000.0),
        "g" to mapOf("g" to 1.0, "kg" to 0.001),
        "lt" to mapOf("lt" to 1.0, "cc" to 1000.0),
        "cc" to mapOf("cc" to 1.0, "lt" to 0.001),
        "un" to mapOf("un" to 1.0)
    )

    fun convert(amount: Double, fromUnit: String, toUnit: String): Double {
        if (fromUnit == toUnit) return amount
        val fromMap = factors[fromUnit]
            ?: error("Unidad origen no soportada: $fromUnit")
        val factor = fromMap[toUnit]
            ?: error("Conversión no soportada: $fromUnit -> $toUnit")
        return amount * factor
    }
}
