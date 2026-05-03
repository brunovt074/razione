package com.recipecostcalculator.domain.usecase

import com.recipecostcalculator.domain.model.CostBreakdown
import com.recipecostcalculator.domain.model.IngredientCostLine
import com.recipecostcalculator.domain.model.Recipe
import com.recipecostcalculator.domain.model.RecipeIngredient
import com.recipecostcalculator.domain.repository.AdditionalVariableCostRepository
import com.recipecostcalculator.domain.repository.FixedCostRepository
import com.recipecostcalculator.domain.repository.RecipeRepository
import com.recipecostcalculator.domain.repository.IngredientRepository
import com.recipecostcalculator.domain.repository.SettingsRepository

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
            ?: error("Receta $recipeId no encontrada")

        val ancestorChain = buildAncestorChain(recipe)

        val allIngredientLines = mutableListOf<IngredientCostLine>()
        var parentCost = 0.0
        var ownCost = 0.0

        ancestorChain.forEachIndexed { depth, chainRecipe ->
            val isThisRecipe = depth == 0
            val ingredients = recipeRepository.getIngredients(chainRecipe.id)
            ingredients.forEach { ri ->
                val ingredient = ingredientRepository.getById(ri.ingredientId) ?: return@forEach
                val cost = calculateIngredientCost(ingredient, ri, settings.wasteFactor)
                allIngredientLines.add(
                    IngredientCostLine(
                        ingredientName = ingredient.name,
                        usageDescription = formatUsage(ri, ingredient),
                        costPerPizza = cost,
                        isFromParentRecipe = !isThisRecipe
                    )
                )
                if (isThisRecipe) ownCost += cost else parentCost += cost
            }
        }

        val totalIngredientCost = parentCost + ownCost
        val additionalCosts = additionalCostRepository.getForRecipe(recipeId)
        val totalAdditional = additionalCosts.sumOf { it.unitCost }
        val totalVariable = totalIngredientCost + totalAdditional

        val totalFixedMonthly = fixedCostRepository.getTotalMonthly()
        val estProduction = settings.estimatedMonthlyProduction
        val fixedPerUnit = if (estProduction > 0) totalFixedMonthly / estProduction else 0.0

        CostBreakdown(
            recipeId = recipe.id,
            recipeName = recipe.name,
            ingredientCostFromParent = parentCost,
            ingredientCostOwn = ownCost,
            totalIngredientCost = totalIngredientCost,
            additionalVariableCost = totalAdditional,
            totalVariableCost = totalVariable,
            fixedCostPerUnit = fixedPerUnit,
            totalCostPerUnit = totalVariable + fixedPerUnit,
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
        wasteFactor: Double
    ): Double {
        val pricePerUsageUnit = ingredient.purchasePrice / ingredient.contentAmount
        val baseCost = when {
            ri.usagePerPizza != null -> pricePerUsageUnit * ri.usagePerPizza
            ri.yieldPizzas != null && ri.yieldPizzas > 0 ->
                ingredient.purchasePrice / ri.yieldPizzas
            else -> 0.0
        }
        return baseCost * (1 + wasteFactor)
    }

    private fun formatUsage(ri: RecipeIngredient, ingredient: com.recipecostcalculator.domain.model.Ingredient): String = when {
        ri.usagePerPizza != null -> "${ri.usagePerPizza} ${ingredient.usageUnit}"
        ri.yieldPizzas != null -> "rinde ${ri.yieldPizzas} pizzas"
        else -> "-"
    }

    companion object {
        private const val MAX_DEPTH = 5
    }
}