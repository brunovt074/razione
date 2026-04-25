package com.recipecostcalculator.domain.model.valueobject

import java.math.BigDecimal
import java.math.RoundingMode

@ConsistentCopyVisibility
data class Money private constructor(
    val amount: BigDecimal,
    val currency: String,
) {
    init {
        require(amount.signum() >= 0) { "El monto no puede ser negativo" }
        require(currency.isNotBlank()) { "La moneda es obligatoria" }
    }

    operator fun plus(other: Money): Money {
        require(currency == other.currency) {
            "No se pueden sumar monedas distintas: $currency y ${other.currency}"
        }
        return of(amount.add(other.amount), currency)
    }

    operator fun times(multiplier: BigDecimal): Money {
        require(multiplier.signum() >= 0) { "El multiplicador no puede ser negativo" }
        return of(amount.multiply(multiplier), currency)
    }

    fun toDisplay(scale: Int = 2): String {
        val value = amount.setScale(scale, RoundingMode.HALF_UP).toPlainString()
        return "$value $currency"
    }

    companion object {
        private const val INTERNAL_SCALE = 6

        fun of(value: BigDecimal, currency: String = "ARS"): Money {
            val normalized = value.setScale(INTERNAL_SCALE, RoundingMode.HALF_UP).stripTrailingZeros()
            return Money(normalized, currency)
        }

        fun of(value: String, currency: String = "ARS"): Money = of(BigDecimal(value), currency)

        fun zero(currency: String = "ARS"): Money = of(BigDecimal.ZERO, currency)
    }
}
