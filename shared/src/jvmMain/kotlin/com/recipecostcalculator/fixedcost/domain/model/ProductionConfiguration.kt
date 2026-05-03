package com.recipecostcalculator.fixedcost.domain.model

import java.math.BigDecimal

data class ProductionConfiguration(
    val pizzasPerMonth: BigDecimal,
) {
    init {
        require(pizzasPerMonth.signum() >= 0) { "La produccion mensual no puede ser negativa" }
    }
}
