package com.recipecostcalculator.ui.screens.pricing

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.recipecostcalculator.pricing.application.dto.PricingSummaryDto
import com.recipecostcalculator.pricing.application.usecase.CalculatePricingSummaryQuery
import com.recipecostcalculator.pricing.application.usecase.CalculatePricingSummaryUseCase
import com.recipecostcalculator.pricing.application.usecase.GetPricingConfigurationUseCase
import com.recipecostcalculator.recipe.application.usecase.GetAllRecipesUseCase
import com.recipecostcalculator.recipe.domain.model.Recipe
import com.recipecostcalculator.ui.components.parseDecimalOrNull
import java.math.BigDecimal
import java.math.RoundingMode

class PricingPresenter(
    private val getAllRecipes: GetAllRecipesUseCase,
    private val getPricingConfigurationUseCase: GetPricingConfigurationUseCase,
    private val calculatePricingSummaryUseCase: CalculatePricingSummaryUseCase,
    private val savePricingConfiguration: (BigDecimal, BigDecimal, BigDecimal, BigDecimal) -> Unit,
) {
    var recipes by mutableStateOf(emptyList<Recipe>())
        private set

    var selectedRecipeId by mutableStateOf<Long?>(null)
        private set

    var sellingPriceInput by mutableStateOf("0")
        private set

    var commissionPercentInput by mutableStateOf("0")
        private set

    var wastePercentInput by mutableStateOf("0")
        private set

    var targetMarginPercentInput by mutableStateOf("50")
        private set

    var summary by mutableStateOf<PricingSummaryDto?>(null)
        private set

    var message by mutableStateOf<String?>(null)
        private set

    init {
        refresh()
    }

    fun selectRecipe(recipe: Recipe) {
        selectedRecipeId = recipe.id
        calculate()
    }

    fun currentSelectedRecipe(): Recipe? {
        val currentId = selectedRecipeId ?: return null
        return recipes.firstOrNull { it.id == currentId }
    }

    fun updateSellingPriceInput(value: String) {
        sellingPriceInput = value
    }

    fun updateCommissionPercentInput(value: String) {
        commissionPercentInput = value
    }

    fun updateWastePercentInput(value: String) {
        wastePercentInput = value
    }

    fun updateTargetMarginPercentInput(value: String) {
        targetMarginPercentInput = value
    }

    fun saveConfiguration() {
        val sellingPrice = parseDecimalOrNull(sellingPriceInput)
        val commissionPercent = parseDecimalOrNull(commissionPercentInput)
        val wastePercent = parseDecimalOrNull(wastePercentInput)
        val targetMarginPercent = parseDecimalOrNull(targetMarginPercentInput)
        if (sellingPrice == null || commissionPercent == null || wastePercent == null || targetMarginPercent == null) {
            message = "Completa precio, comision, merma y margen objetivo validos"
            return
        }
        if (targetMarginPercent >= BigDecimal("100")) {
            message = "El margen objetivo debe ser menor a 100"
            return
        }
        if (commissionPercent >= BigDecimal("100")) {
            message = "La comision/descuento debe ser menor a 100"
            return
        }
        savePricingConfiguration(sellingPrice, commissionPercent, wastePercent, targetMarginPercent)
        message = "Configuracion de precio guardada"
        calculate()
    }

    fun calculate() {
        val recipeId = selectedRecipeId
        if (recipeId == null) {
            summary = null
            message = "Selecciona una receta"
            return
        }
        summary = calculatePricingSummaryUseCase.execute(CalculatePricingSummaryQuery(recipeId = recipeId))
        message = null
    }

    fun refresh() {
        recipes = getAllRecipes.execute()
        if (selectedRecipeId == null) {
            selectedRecipeId = recipes.firstOrNull()?.id
        }
        val configuration = getPricingConfigurationUseCase.execute()
        sellingPriceInput = configuration.sellingPricePerPizza.amount.stripTrailingZeros().toPlainString()
        commissionPercentInput = configuration.commissionPercent.stripTrailingZeros().toPlainString()
        wastePercentInput = configuration.wastePercent.stripTrailingZeros().toPlainString()
        targetMarginPercentInput = configuration.targetMarginPercent.stripTrailingZeros().toPlainString()
        if (selectedRecipeId != null) {
            calculate()
        } else {
            message = "Crea una receta para poder calcular precio y margen"
        }
    }

    fun toPercentText(value: BigDecimal): String = value.setScale(2, RoundingMode.HALF_UP).toPlainString()
}
