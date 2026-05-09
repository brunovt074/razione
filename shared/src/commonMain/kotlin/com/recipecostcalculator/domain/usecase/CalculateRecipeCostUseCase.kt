package com.recipecostcalculator.domain.usecase

import com.recipecostcalculator.domain.model.CostBreakdown
import com.recipecostcalculator.domain.model.IngredientCostLine
import com.recipecostcalculator.domain.model.IngredientUsageMode
import com.recipecostcalculator.domain.model.Recipe
import com.recipecostcalculator.domain.model.RecipeIngredient
import com.recipecostcalculator.domain.repository.AdditionalVariableCostRepository
import com.recipecostcalculator.domain.repository.FixedCostRepository
import com.recipecostcalculator.domain.repository.RecipeRepository
import com.recipecostcalculator.domain.repository.IngredientRepository
import com.recipecostcalculator.domain.repository.SettingsRepository
import com.recipecostcalculator.financial.domain.model.Money
import com.recipecostcalculator.financial.domain.model.Percentage
import com.recipecostcalculator.financial.domain.model.Quantity

class CalculateRecipeCostUseCase(
    private val recipeRepository: RecipeRepository,
    private val ingredientRepository: IngredientRepository,
    private val additionalCostRepository: AdditionalVariableCostRepository,
    private val fixedCostRepository: FixedCostRepository,
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(recipeId: Long): Result<CostBreakdown> = runCatching {
        val settings = settingsRepository.getSettings()
        val recipe = recipeRepository.getById(recipeId)
            ?: error("Recipe $recipeId not found")

        val ancestorChain = buildAncestorChain(recipe)
        val wasteFactor = Percentage(settings.wasteFactor)

        val allIngredientLines = mutableListOf<IngredientCostLine>()
        var parentCost = Money.ZERO
        var ownCost = Money.ZERO

        ancestorChain.forEachIndexed { depth, chainRecipe ->
            val isThisRecipe = depth == 0
            val ingredients = recipeRepository.getIngredients(chainRecipe.id)
            ingredients.forEach { ri ->
                val ingredient = ingredientRepository.getById(ri.ingredientId) ?: return@forEach
                val cost = calculateIngredientCost(ingredient, ri, wasteFactor)
                allIngredientLines.add(
                    IngredientCostLine(
                        ingredientId = ri.ingredientId,
                        ingredientName = ingredient.name,
                        usageDescription = formatUsage(ri, ingredient),
                        costPerPizza = cost,
                        isFromParentRecipe = !isThisRecipe,
                        parentRecipeName = if (!isThisRecipe) chainRecipe.name else null
                    )
                )
                if (isThisRecipe) ownCost += cost else parentCost += cost
            }
        }

        val totalIngredientCost = parentCost + ownCost
        val additionalCosts = additionalCostRepository.getForRecipe(recipeId)
        var totalAdditional = Money.ZERO
        additionalCosts.forEach { extra ->
            totalAdditional += extra.unitCost
        }
        val totalVariable = totalIngredientCost + totalAdditional

        val totalFixedMonthly = fixedCostRepository.getTotalMonthly()
        val estProduction = settings.estimatedMonthlyProduction
        val fixedPerUnit = if (estProduction > 0) totalFixedMonthly / estProduction else Money.ZERO

        val totalVariableCost = totalVariable
        val totalCostPerUnit = totalVariableCost + fixedPerUnit
        val batchCost = totalVariable * settings.batchSize

        CostBreakdown(
            recipeId = recipe.id,
            recipeName = recipe.name,
            ingredientCostFromParent = parentCost,
            ingredientCostOwn = ownCost,
            totalIngredientCost = totalIngredientCost,
            additionalVariableCost = totalAdditional,
            totalVariableCost = totalVariableCost,
            fixedCostPerUnit = fixedPerUnit,
            totalCostPerUnit = totalCostPerUnit,
            batchCost = batchCost,
            ingredientBreakdown = allIngredientLines
        )
    }

    private suspend fun buildAncestorChain(recipe: Recipe, depth: Int = 0): List<Recipe> {
        if (depth >= MAX_DEPTH) return listOf(recipe)
        val chain = mutableListOf(recipe)
        recipe.parentRecipeId?.let { parentId ->
            val parent = recipeRepository.getById(parentId) ?: return chain
            chain.addAll(buildAncestorChain(parent, depth + 1))
        }
        return chain
    }

    private fun calculateIngredientCost(
        ingredient: com.recipecostcalculator.domain.model.Ingredient,
        ri: RecipeIngredient,
        wasteFactor: Percentage
    ): Money {
        val pricePerUnit = ingredient.purchasePrice / ingredient.contentAmount
        val baseCost = when (ri.primaryMode) {
            is IngredientUsageMode.ByUsage -> {
                pricePerUnit * ri.primaryMode.amountPerPizza.value
            }
            is IngredientUsageMode.ByYield -> {
                ingredient.purchasePrice / ri.primaryMode.pizzasPerPurchaseUnit
            }
        }
        val wasteMultiplier = 1.0 + wasteFactor.value
        return baseCost * wasteMultiplier
    }

    private fun formatUsage(ri: RecipeIngredient, ingredient: com.recipecostcalculator.domain.model.Ingredient): String = when (ri.primaryMode) {
        is IngredientUsageMode.ByUsage -> "${ri.primaryMode.amountPerPizza.value} ${ingredient.usageUnit}"
        is IngredientUsageMode.ByYield -> "rinde ${ri.primaryMode.pizzasPerPurchaseUnit} pizzas"
    }

    companion object {
        private const val MAX_DEPTH = 5
    }
}
