package com.recipecostcalculator.pricing.application.usecase

import com.recipecostcalculator.costing.domain.service.RecipeCostCalculator
import com.recipecostcalculator.financial.domain.model.Money
import com.recipecostcalculator.ingredient.domain.repository.IngredientRepository
import com.recipecostcalculator.pricing.domain.model.PriceAnalysis
import com.recipecostcalculator.pricing.domain.service.PricingCalculator
import com.recipecostcalculator.recipe.domain.repository.RecipeRepository
import com.recipecostcalculator.settings.domain.repository.SettingsRepository
import com.recipecostcalculator.fixedcost.domain.repository.FixedCostRepository

class AnalyzePriceUseCase(
    private val recipeRepository: RecipeRepository,
    private val ingredientRepository: IngredientRepository,
    private val additionalCostRepository: com.recipecostcalculator.costing.domain.repository.AdditionalVariableCostRepository,
    private val fixedCostRepository: FixedCostRepository,
    private val settingsRepository: SettingsRepository,
    private val costCalculator: RecipeCostCalculator,
    private val pricingCalculator: PricingCalculator,
) {
    data class Command(
        val recipeId: Long,
        val salePrice: Double,
    )

    suspend fun execute(command: Command): Result<PriceAnalysis> = runCatching {
        val settings = settingsRepository.getSettings()

        val recipe = recipeRepository.findById(command.recipeId)
            ?: error("Receta ${command.recipeId} no encontrada")

        val ancestorChain = recipeRepository.findAncestorChain(command.recipeId)
            .drop(1)

        val allIngredientIds = (recipe.ingredients.map { it.ingredientId } +
            ancestorChain.flatMap { it.ingredients.map { ri -> ri.ingredientId } }
        ).toSet()

        val ingredientMap = allIngredientIds
            .mapNotNull { id -> ingredientRepository.findById(id)?.let { id to it } }
            .toMap()

        val additionalCosts = additionalCostRepository.findForRecipe(command.recipeId)

        val totalFixed = fixedCostRepository.getTotalMonthly()
        val fixedPerUnit = if (settings.estimatedMonthlyProduction > 0)
            Money.of(totalFixed / settings.estimatedMonthlyProduction)
        else
            Money.ZERO

        val breakdown = costCalculator.calculate(
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

        pricingCalculator.analyze(
            breakdown = breakdown,
            salePrice = Money.of(command.salePrice),
            commissionPct = settings.commissionPct,
            targetMargin = settings.targetMargin,
            costMode = settings.defaultCostMode,
        )
    }
}