package com.recipecostcalculator.fixedcost.domain.model

import com.recipecostcalculator.financial.domain.model.Money

data class FixedCostItem(
    val id: Long = 0,
    val name: String,
    val monthlyAmount: Money,
) {
    init {
        require(name.isNotBlank()) { "El concepto es obligatorio" }
        require(monthlyAmount.amount.signum() >= 0) { "El monto mensual no puede ser negativo" }
    }
}
