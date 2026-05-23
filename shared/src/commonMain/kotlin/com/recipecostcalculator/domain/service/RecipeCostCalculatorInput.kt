package com.recipecostcalculator.domain.service

import com.recipecostcalculator.domain.model.AdditionalVariableCost
import com.recipecostcalculator.domain.model.Ingredient
import com.recipecostcalculator.domain.model.Recipe
import com.recipecostcalculator.financial.domain.model.Money
import com.recipecostcalculator.financial.domain.model.Percentage

data class RecipeCostCalculatorInput(
    val recipe: Recipe,
    val ancestorChain: List<Recipe>,
    val ingredientMap: Map<Long, Ingredient>,
    val additionalCosts: List<AdditionalVariableCost>,
    val wasteFactor: Percentage,
    val fixedCostPerUnit: Money,
    val batchSize: Int,
)
