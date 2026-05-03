package com.recipecostcalculator.fixedcost.application.usecase

import java.math.BigDecimal

data class SetProductionPerMonthCommand(
    val pizzasPerMonth: BigDecimal,
)
