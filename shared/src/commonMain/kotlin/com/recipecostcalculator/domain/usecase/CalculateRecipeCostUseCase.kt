package com.recipecostcalculator.domain.usecase

import com.recipecostcalculator.domain.model.CostBreakdown
import com.recipecostcalculator.domain.model.Ingredient
import com.recipecostcalculator.domain.model.Recipe
import com.recipecostcalculator.domain.repository.AdditionalVariableCostRepository
import com.recipecostcalculator.domain.repository.FixedCostRepository
import com.recipecostcalculator.domain.repository.RecipeRepository
import com.recipecostcalculator.domain.repository.IngredientRepository
import com.recipecostcalculator.domain.repository.SettingsRepository
import com.recipecostcalculator.domain.service.RecipeCostCalculator
import com.recipecostcalculator.financial.domain.model.Money
import java.math.BigDecimal

class CalculateRecipeCostUseCase(
    private val recipeRepository: RecipeRepository,
    private val ingredientRepository: IngredientRepository,
    private val additionalCostRepository: AdditionalVariableCostRepository,
    private val fixedCostRepository: FixedCostRepository,
    private val settingsRepository: SettingsRepository,
    private val calculator: RecipeCostCalculator
) {
    suspend operator fun invoke(recipeId: Long): Result<CostBreakdown> = runCatching {
        val settings = settingsRepository.getSettings()

        val recipe = recipeRepository.getById(recipeId)
            ?: error("Receta $recipeId no encontrada")

        val ancestorChain = buildAncestorChain(recipe)
            .drop(1)

        val allIngredientIds = (recipe.recipeIngredients.map { it.ingredientId } +
                ancestorChain.flatMap { it.recipeIngredients.map { ri -> ri.ingredientId } }
                ).toSet()

        val ingredientMap = allIngredientIds
            .mapNotNull { id -> ingredientRepository.getById(id)?.let { id to it } }
            .toMap()

        val additionalCosts = additionalCostRepository.getForRecipe(recipeId)

        val totalFixedMonthly = fixedCostRepository.getTotalMonthly()
        val fixedPerUnit = if (settings.estimatedMonthlyProduction > 0) {
            Money.of(totalFixedMonthly.amount.toDouble() / settings.estimatedMonthlyProduction)
        } else {
            Money.ZERO
        }

        calculator.calculate(
            RecipeCostCalculator.Input(
                recipe = recipe,
                ancestorChain = ancestorChain,
                ingredientMap = ingredientMap,
                additionalCosts = additionalCosts,
                wasteFactor = settings.wasteFactor,
                fixedCostPerUnit = fixedPerUnit,
                batchSize = settings.batchSize,
            )
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

    companion object {
        private const val MAX_DEPTH = 5
    }
}