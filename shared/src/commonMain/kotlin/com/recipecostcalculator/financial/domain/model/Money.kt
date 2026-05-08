package com.recipecostcalculator.financial.domain.model

data class Money(val amount: Double) {
    init {
        require(!amount.isNaN()) { "Money amount cannot be NaN" }
        require(!amount.isInfinite()) { "Money amount cannot be Infinite" }
    }

    operator fun plus(other: Money): Money = Money(round(amount + other.amount))
    operator fun minus(other: Money): Money = Money(round(amount - other.amount))
    operator fun times(factor: Double): Money = Money(round(amount * factor))
    operator fun times(factor: Int): Money = Money(round(amount * factor))
    operator fun div(divisor: Double): Money {
        require(divisor != 0.0) { "Cannot divide Money by zero" }
        return Money(round(amount / divisor))
    }
    operator fun div(divisor: Int): Money {
        require(divisor != 0) { "Cannot divide Money by zero" }
        return Money(round(amount / divisor.toDouble()))
    }
    operator fun unaryMinus(): Money = Money(-amount)
    operator fun compareTo(other: Money): Int = amount.compareTo(other.amount)

    fun isZero(): Boolean = amount == 0.0

    private fun round(value: Double): Double = kotlin.math.round(value * 100) / 100.0

    companion object {
        val ZERO = Money(0.0)
        fun of(amount: Double): Money = Money(kotlin.math.round(amount * 100) / 100.0)
        fun of(amount: Int): Money = Money(amount.toDouble())
    }
}