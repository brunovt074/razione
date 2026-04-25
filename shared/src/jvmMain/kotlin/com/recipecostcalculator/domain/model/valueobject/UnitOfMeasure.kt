package com.recipecostcalculator.domain.model.valueobject

import java.math.BigDecimal

enum class UnitCategory {
    MASS,
    VOLUME,
    COUNT,
}

enum class UnitOfMeasure(
    val symbol: String,
    val category: UnitCategory,
    val toBaseFactor: BigDecimal,
) {
    GRAM(symbol = "g", category = UnitCategory.MASS, toBaseFactor = BigDecimal.ONE),
    KILOGRAM(symbol = "kg", category = UnitCategory.MASS, toBaseFactor = BigDecimal("1000")),
    MILLILITER(symbol = "ml", category = UnitCategory.VOLUME, toBaseFactor = BigDecimal.ONE),
    LITER(symbol = "l", category = UnitCategory.VOLUME, toBaseFactor = BigDecimal("1000")),
    UNIT(symbol = "unit", category = UnitCategory.COUNT, toBaseFactor = BigDecimal.ONE),
    ;

    fun isCompatibleWith(other: UnitOfMeasure): Boolean = category == other.category

    companion object {
        fun fromRaw(value: String): UnitOfMeasure {
            val normalized = value.trim().lowercase()
            return entries.firstOrNull {
                it.symbol.lowercase() == normalized || it.name.lowercase() == normalized
            } ?: throw IllegalArgumentException("Unidad no soportada: $value")
        }
    }
}
