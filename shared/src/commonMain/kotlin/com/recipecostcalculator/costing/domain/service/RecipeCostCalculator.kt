package com.recipecostcalculator.costing.domain.service

import com.recipecostcalculator.costing.domain.model.AdditionalVariableCost
import com.recipecostcalculator.costing.domain.model.CostBreakdown
import com.recipecostcalculator.costing.domain.model.IngredientCostLine
import com.recipecostcalculator.financial.domain.model.Money
import com.recipecostcalculator.financial.domain.model.Percentage
import com.recipecostcalculator.ingredient.domain.model.Ingredient
import com.recipecostcalculator.recipe.domain.model.Recipe

class RecipeCostCalculator(
    private val ingredientCostCalculator: IngredientCostCalculator,
) {
    data class Input(
        val recipe: Recipe,
        val ancestorChain: List<Recipe>,
        val ingredientMap: Map<Long, Ingredient>,
        val additionalCosts: List<AdditionalVariableCost>,
        val wasteFactor: Percentage,
        val fixedCostPerUnit: Money,
        val batchSize: Int,
    )

    fun calculate(input: Input): CostBreakdown {
        val allLines = mutableListOf<IngredientCostLine>()
        var parentCost = Money.ZERO
        var ownCost = Money.ZERO

        input.recipe.ingredients.forEach { ri ->
            val ingredient = input.ingredientMap[ri.ingredientId] ?: return@forEach
            val cost = ingredientCostCalculator.calculate(
                ingredient, ri.primaryMode, input.wasteFactor
            )
            ownCost += cost
            allLines += IngredientCostLine(
                ingredientId = ri.ingredientId,
                ingredientName = ingredient.name,
                usageDescription = ingredientCostCalculator.formatUsageDescription(
                    ri.primaryMode, ingredient.usageUnit.label
                ),
                costPerPizza = cost,
                isFromParentRecipe = false,
                parentRecipeName = null,
            )
        }

        input.ancestorChain.forEach { ancestor ->
            ancestor.ingredients.forEach { ri ->
                val ingredient = input.ingredientMap[ri.ingredientId] ?: return@forEach
                val cost = ingredientCostCalculator.calculate(
                    ingredient, ri.primaryMode, input.wasteFactor
                )
                parentCost += cost
                allLines += IngredientCostLine(
                    ingredientId = ri.ingredientId,
                    ingredientName = ingredient.name,
                    usageDescription = ingredientCostCalculator.formatUsageDescription(
                        ri.primaryMode, ingredient.usageUnit.label
                    ),
                    costPerPizza = cost,
                    isFromParentRecipe = true,
                    parentRecipeName = ancestor.name,
                )
            }
        }

        val totalIngredients = parentCost + ownCost

        var totalAdditional = Money.ZERO
        input.additionalCosts.forEach { extra ->
            val costWithWaste = extra.unitCost * (1.0 + input.wasteFactor.toDouble())
            totalAdditional += costWithWaste
        }

        val totalVariable = totalIngredients + totalAdditional
        val totalCostPerUnit = totalVariable + input.fixedCostPerUnit
        val batchCost = totalVariable * input.batchSize

        return CostBreakdown(
            recipeId = input.recipe.id,
            recipeName = input.recipe.name,
            ingredientCostFromParent = parentCost,
            ingredientCostOwn = ownCost,
            totalIngredientCost = totalIngredients,
            additionalVariableCost = totalAdditional,
            totalVariableCost = totalVariable,
            fixedCostPerUnit = input.fixedCostPerUnit,
            totalCostPerUnit = totalCostPerUnit,
            batchCost = batchCost,
            ingredientLines = allLines,
        )
    }
}