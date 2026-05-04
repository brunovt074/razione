package com.recipecostcalculator.financial.domain.model

import java.math.BigDecimal
import java.math.RoundingMode

data class Money(val amount: BigDecimal) {

    init {
        require(amount.compareTo(BigDecimal.ZERO) != -1) { "Money amount cannot be negative" }
    }

    operator fun plus(other: Money): Money =
        Money(amount.add(other.amount).setScale(2, RoundingMode.HALF_UP))

    operator fun minus(other: Money): Money =
        Money(amount.subtract(other.amount).setScale(2, RoundingMode.HALF_UP))

    operator fun times(factor: BigDecimal): Money =
        Money(amount.multiply(factor).setScale(2, RoundingMode.HALF_UP))

    operator fun times(factor: Double): Money =
        Money(amount.multiply(BigDecimal(factor)).setScale(2, RoundingMode.HALF_UP))

    operator fun times(factor: Int): Money =
        Money(amount.multiply(BigDecimal(factor)).setScale(2, RoundingMode.HALF_UP))

    operator fun div(divisor: BigDecimal): Money {
        require(divisor.compareTo(BigDecimal.ZERO) != 0) { "Cannot divide Money by zero" }
        return Money(amount.divide(divisor, 2, RoundingMode.HALF_UP))
    }

    operator fun div(divisor: Double): Money {
        require(divisor != 0.0) { "Cannot divide Money by zero" }
        return Money(amount.divide(BigDecimal(divisor), 2, RoundingMode.HALF_UP))
    }

    operator fun div(divisor: Int): Money {
        require(divisor != 0) { "Cannot divide Money by zero" }
        return Money(amount.divide(BigDecimal(divisor), 2, RoundingMode.HALF_UP))
    }

    operator fun unaryMinus(): Money = Money(amount.negate())

    operator fun compareTo(other: Money): Int = amount.compareTo(other.amount)

    fun isZero(): Boolean = amount.compareTo(BigDecimal.ZERO) == 0

    fun isPositive(): Boolean = amount.compareTo(BigDecimal.ZERO) > 0

    fun toDouble(): Double = amount.toDouble()

    fun toScaledString(): String = amount.setScale(2, RoundingMode.HALF_UP).toPlainString()

    companion object {
        val ZERO = Money(BigDecimal.ZERO)

        fun of(amount: Double): Money =
            Money(BigDecimal.valueOf(amount).setScale(2, RoundingMode.HALF_UP))

        fun of(amount: BigDecimal): Money =
            Money(amount.setScale(2, RoundingMode.HALF_UP))

        fun of(amount: Int): Money = Money(BigDecimal(amount))

        fun fromString(value: String): Money =
            Money(BigDecimal(value).setScale(2, RoundingMode.HALF_UP))
    }
}