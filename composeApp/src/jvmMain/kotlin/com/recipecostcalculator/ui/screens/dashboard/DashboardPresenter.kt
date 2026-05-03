package com.recipecostcalculator.ui.screens.dashboard

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.recipecostcalculator.costing.application.dto.RecipeCostDto
import com.recipecostcalculator.costing.application.usecase.CalculateRecipeCostQuery
import com.recipecostcalculator.costing.domain.service.CostResult
import com.recipecostcalculator.fixedcost.application.usecase.CalculateFixedCostSummaryUseCase
import com.recipecostcalculator.pricing.application.usecase.CalculatePricingSummaryQuery
import com.recipecostcalculator.pricing.application.usecase.CalculatePricingSummaryUseCase
import com.recipecostcalculator.pricing.application.usecase.GetPricingConfigurationUseCase
import com.recipecostcalculator.recipe.application.usecase.GetAllRecipesUseCase
import com.recipecostcalculator.recipe.domain.model.Recipe
import java.math.BigDecimal
import java.math.RoundingMode

class DashboardPresenter(
    private val getAllRecipes: GetAllRecipesUseCase,
    private val calculateRecipeCost: (CalculateRecipeCostQuery) -> CostResult,
    private val calculateFixedCostSummaryUseCase: CalculateFixedCostSummaryUseCase,
    private val getPricingConfigurationUseCase: GetPricingConfigurationUseCase,
    private val calculatePricingSummaryUseCase: CalculatePricingSummaryUseCase,
) {
    var recipes by mutableStateOf(emptyList<Recipe>())
        private set

    var selectedRecipeId by mutableStateOf<Long?>(null)
        private set

    var latestCost by mutableStateOf<RecipeCostDto?>(null)
        private set

    var loteQuantityInput by mutableStateOf("6")
        private set

    var message by mutableStateOf<String?>(null)
        private set

    init {
        refresh()
        calculateSelectedRecipeCost()
    }

    fun refresh() {
        recipes = getAllRecipes.execute()
        if (selectedRecipeId == null) {
            selectedRecipeId = recipes.firstOrNull()?.id
        }
        if (recipes.isEmpty()) {
            message = "Todavia no hay recetas. Crea una para empezar"
        }
    }

    fun updateLoteQuantityInput(value: String) {
        loteQuantityInput = value
    }

    fun selectRecipe(recipe: Recipe) {
        selectedRecipeId = recipe.id
        calculateSelectedRecipeCost()
    }

    fun currentSelectedRecipe(): Recipe? {
        val currentId = selectedRecipeId ?: return null
        return recipes.firstOrNull { it.id == currentId }
    }

    fun calculateSelectedRecipeCost() {
        val recipeId = selectedRecipeId
        if (recipeId == null) {
            latestCost = null
            message = "No hay recetas para calcular"
            return
        }

        val result = calculateRecipeCost(CalculateRecipeCostQuery(recipeId = recipeId))
        latestCost = RecipeCostDto.from(result)
        message = null
    }

    fun kpiCostoVariablePorPizza(): String = latestCost?.costPerUnit ?: "0.00 ARS"

    fun kpiCostoVariablePorLote(): String {
        val dto = latestCost ?: return "0.00 ARS"
        val baseValue = parseMoneyAmount(dto.costPerUnit) ?: return "0.00 ARS"
        val loteQuantity = loteQuantityInput.trim().toBigDecimalOrNull() ?: BigDecimal.ZERO
        val loteValue = baseValue.multiply(loteQuantity).setScale(2, RoundingMode.HALF_UP)
        return "${loteValue.toPlainString()} ARS"
    }

    private fun currentRecipeId(): Long? = selectedRecipeId

    private fun currentPricingSummaryOrNull() = currentRecipeId()?.let { recipeId ->
        calculatePricingSummaryUseCase.execute(CalculatePricingSummaryQuery(recipeId = recipeId))
    }

    fun buildKpi(): DashboardKpi {
        val fixedSummary = calculateFixedCostSummaryUseCase.execute()
        val pricingConfiguration = getPricingConfigurationUseCase.execute()
        val pricingSummary = currentPricingSummaryOrNull()

        val totalCostPerPizza = pricingSummary?.totalCostPerPizza?.toDisplay() ?: "0.00 ARS"
        val grossProfit = pricingSummary?.grossProfit?.toDisplay() ?: "0.00 ARS"
        val grossMarginPercent = pricingSummary?.grossMarginPercent
            ?.setScale(2, RoundingMode.HALF_UP)
            ?.toPlainString()
            ?: "0.00"
        val suggestedPriceTarget = pricingSummary?.suggestedPriceWithCommission?.toDisplay() ?: "0.00 ARS"

        return DashboardKpi(
            variableCostPerPizza = kpiCostoVariablePorPizza(),
            variableCostPerLote = kpiCostoVariablePorLote(),
            fixedCostMonthly = fixedSummary.totalMonthlyFixedCost.toDisplay(),
            fixedCostPerPizza = fixedSummary.fixedCostPerPizza.toDisplay(),
            totalCostPerPizza = totalCostPerPizza,
            sellingPricePerPizza = pricingConfiguration.sellingPricePerPizza.toDisplay(),
            commissionPercent = pricingConfiguration.commissionPercent.setScale(2, RoundingMode.HALF_UP).toPlainString(),
            grossProfit = grossProfit,
            grossMarginPercent = grossMarginPercent,
            suggestedPriceTarget = suggestedPriceTarget,
        )
    }

    private fun parseMoneyAmount(raw: String): BigDecimal? {
        val firstToken = raw.trim().split(" ").firstOrNull() ?: return null
        return firstToken.toBigDecimalOrNull()
    }
}
