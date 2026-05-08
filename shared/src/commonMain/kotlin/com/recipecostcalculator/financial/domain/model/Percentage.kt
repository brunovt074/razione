package com.recipecostcalculator.financial.domain.model

data class Percentage(val value: Double) {
    init {
        require(value in 0.0..1.0) {
            "Percentage must be between 0.0 and 1.0, got $value"
        }
    }

    fun complement(): Double = 1.0 - value
    fun complementIsZero(): Boolean = complement() == 0.0
    fun toDouble(): Double = value
    operator fun plus(other: Percentage): Percentage = Percentage(value + other.value)

    companion object {
        val ZERO = Percentage(0.0)
        fun fromPercent(pct: Double): Percentage = Percentage(pct / 100.0)
    }
}