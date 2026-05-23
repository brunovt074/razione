package com.recipecostcalculator.domain.model

import com.recipecostcalculator.financial.domain.model.Quantity

data class ByUsage(val amountPerPizza: Quantity) : IngredientUsageMode()
