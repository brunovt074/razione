package com.recipecostcalculator.financial.domain.model

import java.math.BigDecimal

data class Quantity(val value: BigDecimal) {

    init {
        require(value.compareTo(BigDecimal.ZERO) > 0) { "Quantity must be positive" }
    }

    operator fun div(other: Quantity): BigDecimal =
        value.divide(other.value, 4, java.math.RoundingMode.HALF_UP)

    operator fun times(money: Money): Money = money * value.toDouble()

    fun toDouble(): Double = value.toDouble()

    fun toScaledString(): String = value.setScale(3, java.math.RoundingMode.HALF_UP).toPlainString()

    companion object {
        fun of(value: Double): Quantity =
            Quantity(BigDecimal.valueOf(value).setScale(3, java.math.RoundingMode.HALF_UP))

        fun of(value: Int): Quantity = Quantity(BigDecimal(value))

        fun of(value: BigDecimal): Quantity =
            Quantity(value.setScale(3, java.math.RoundingMode.HALF_UP))
    }
}