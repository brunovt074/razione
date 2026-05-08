package com.recipecostcalculator.costing.application.usecase

import com.recipecostcalculator.costing.domain.model.CostBreakdown
import com.recipecostcalculator.costing.domain.repository.AdditionalVariableCostRepository
import com.recipecostcalculator.costing.domain.service.RecipeCostCalculator
import com.recipecostcalculator.financial.domain.model.Money
import com.recipecostcalculator.ingredient.domain.repository.IngredientRepository
import com.recipecostcalculator.recipe.domain.repository.RecipeRepository
import com.recipecostcalculator.settings.domain.model.AppSettings
import com.recipecostcalculator.settings.domain.repository.SettingsRepository
import com.recipecostcalculator.fixedcost.domain.repository.FixedCostRepository

class CalculateRecipeCostUseCase(
    private val recipeRepository: RecipeRepository,
    private val ingredientRepository: IngredientRepository,
    private val additionalCostRepository: AdditionalVariableCostRepository,
    private val fixedCostRepository: FixedCostRepository,
    private val settingsRepository: SettingsRepository,
    private val calculator: RecipeCostCalculator,
) {
    suspend fun execute(recipeId: Long): Result<CostBreakdown> = runCatching {
        val settings = settingsRepository.getSettings()

        val recipe = recipeRepository.findById(recipeId)
            ?: error("Receta $recipeId no encontrada")

        val ancestorChain = recipeRepository.findAncestorChain(recipeId)
            .drop(1)

        val allIngredientIds = (recipe.ingredients.map { it.ingredientId } +
            ancestorChain.flatMap { it.ingredients.map { ri -> ri.ingredientId } }
        ).toSet()

        val ingredientMap = allIngredientIds
            .mapNotNull { id -> ingredientRepository.findById(id)?.let { id to it } }
            .toMap()

        val additionalCosts = additionalCostRepository.findForRecipe(recipeId)

        val totalFixed = fixedCostRepository.getTotalMonthly()
        val fixedPerUnit = if (settings.estimatedMonthlyProduction > 0)
            Money.of(totalFixed / settings.estimatedMonthlyProduction)
        else
            Money.ZERO

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
}