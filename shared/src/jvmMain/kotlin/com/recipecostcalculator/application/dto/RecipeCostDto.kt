package com.recipecostcalculator.application.dto

import com.recipecostcalculator.domain.service.CostResult

data class IngredientBreakdownDto(
    val ingredientName: String,
    val quantity: String,
    val cost: String,
)

data class RecipeCostDto(
    val recipeName: String,
    val totalCost: String,
    val costPerUnit: String,
    val breakdown: List<IngredientBreakdownDto>,
) {
    companion object {
        fun from(result: CostResult): RecipeCostDto {
            return RecipeCostDto(
                recipeName = result.recipeName,
                totalCost = result.totalCost.toDisplay(),
                costPerUnit = result.costPerUnit.toDisplay(),
                breakdown = result.breakdown.map {
                    IngredientBreakdownDto(
                        ingredientName = it.ingredientName,
                        quantity = it.quantity.toDisplay(),
                        cost = it.cost.toDisplay(),
                    )
                },
            )
        }
    }
}
