package com.recipecostcalculator.financial.domain.model

import java.math.BigDecimal

data class Percentage(val value: BigDecimal) {

    init {
        require(value.compareTo(BigDecimal.ZERO) >= 0) { "Percentage cannot be negative" }
        require(value.compareTo(BigDecimal.ONE) <= 1) { "Percentage cannot exceed 100%" }
    }

    fun complement(): BigDecimal = BigDecimal.ONE.subtract(value)

    fun complementIsZero(): Boolean = complement().compareTo(BigDecimal.ZERO) == 0

    fun toDouble(): Double = value.toDouble()

    fun toPercentString(): String = "${value.multiply(BigDecimal(100)).setScale(1).toPlainString()}%"

    operator fun plus(other: Percentage): Percentage =
        Percentage(value.add(other.value))

    operator fun times(other: BigDecimal): BigDecimal =
        value.multiply(other)

    companion object {
        val ZERO = Percentage(BigDecimal.ZERO)
        val HUNDRED = Percentage(BigDecimal.ONE)

        fun fromPercent(percent: Double): Percentage =
            Percentage(BigDecimal.valueOf(percent).divide(BigDecimal(100), 4, java.math.RoundingMode.HALF_UP))

        fun fromDecimal(decimal: BigDecimal): Percentage =
            Percentage(decimal.setScale(4, java.math.RoundingMode.HALF_UP))
    }
}