package com.recipecostcalculator.pricing.domain.model

import com.recipecostcalculator.financial.domain.model.Money
import java.math.BigDecimal

data class PricingConfiguration(
    val sellingPricePerPizza: Money,
    val commissionPercent: BigDecimal,
    val wastePercent: BigDecimal,
    val targetMarginPercent: BigDecimal,
) {
    init {
        require(commissionPercent.signum() >= 0) { "La comision no puede ser negativa" }
        require(wastePercent.signum() >= 0) { "La merma no puede ser negativa" }
        require(targetMarginPercent.signum() >= 0) { "El margen objetivo no puede ser negativo" }
        require(targetMarginPercent < BigDecimal("100")) { "El margen objetivo debe ser menor a 100" }
        require(commissionPercent < BigDecimal("100")) { "La comision debe ser menor a 100" }
    }

    companion object {
        fun defaults(): PricingConfiguration {
            return PricingConfiguration(
                sellingPricePerPizza = Money.zero(),
                commissionPercent = BigDecimal.ZERO,
                wastePercent = BigDecimal.ZERO,
                targetMarginPercent = BigDecimal("50"),
            )
        }
    }
}
