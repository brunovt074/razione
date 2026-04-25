package com.recipecostcalculator.domain.model.valueobject

import java.math.BigDecimal
import java.math.RoundingMode

@ConsistentCopyVisibility
data class Quantity private constructor(
    val value: BigDecimal,
    val unit: UnitOfMeasure,
) {
    init {
        require(value.signum() >= 0) { "La cantidad no puede ser negativa" }
    }

    fun to(targetUnit: UnitOfMeasure): Quantity {
        require(unit.isCompatibleWith(targetUnit)) {
            "No se puede convertir ${unit.symbol} a ${targetUnit.symbol}"
        }
        if (unit == targetUnit) {
            return this
        }

        val baseValue = value.multiply(unit.toBaseFactor)
        val converted = baseValue.divide(targetUnit.toBaseFactor, INTERNAL_SCALE, RoundingMode.HALF_UP)
        return of(converted, targetUnit)
    }

    operator fun plus(other: Quantity): Quantity {
        require(unit.isCompatibleWith(other.unit)) { "No se pueden sumar unidades incompatibles" }
        val normalized = other.to(unit)
        return of(value.add(normalized.value), unit)
    }

    operator fun minus(other: Quantity): Quantity {
        require(unit.isCompatibleWith(other.unit)) { "No se pueden restar unidades incompatibles" }
        val normalized = other.to(unit)
        val result = value.subtract(normalized.value)
        require(result.signum() >= 0) { "El resultado de la resta no puede ser negativo" }
        return of(result, unit)
    }

    operator fun times(multiplier: BigDecimal): Quantity {
        require(multiplier.signum() >= 0) { "El multiplicador no puede ser negativo" }
        return of(value.multiply(multiplier), unit)
    }

    fun toDisplay(scale: Int = 3): String {
        val text = value.setScale(scale, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString()
        return "$text ${unit.symbol}"
    }

    companion object {
        private const val INTERNAL_SCALE = 6

        fun of(value: BigDecimal, unit: UnitOfMeasure): Quantity {
            val normalized = value.setScale(INTERNAL_SCALE, RoundingMode.HALF_UP).stripTrailingZeros()
            return Quantity(normalized, unit)
        }

        fun of(value: String, unit: UnitOfMeasure): Quantity = of(BigDecimal(value), unit)
    }
}
