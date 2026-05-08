package com.recipecostcalculator.financial.domain.model

data class Quantity(val value: Double) {
    init {
        require(value > 0.0) { "Quantity must be positive, got $value" }
    }

    operator fun div(other: Quantity): Double = value / other.value

    companion object {
        fun of(value: Double): Quantity = Quantity(value)
        fun of(value: Int): Quantity = Quantity(value.toDouble())
    }
}