package com.recipecostcalculator.pricing.application.usecase

import com.recipecostcalculator.costing.application.usecase.CalculateRecipeCostUseCase
import com.recipecostcalculator.costing.domain.service.CostCalculatorService
import com.recipecostcalculator.financial.domain.model.Money
import com.recipecostcalculator.fixedcost.FakeFixedCostRepository
import com.recipecostcalculator.fixedcost.application.usecase.AddFixedCostItemCommand
import com.recipecostcalculator.fixedcost.application.usecase.AddFixedCostItemUseCase
import com.recipecostcalculator.fixedcost.application.usecase.CalculateFixedCostSummaryUseCase
import com.recipecostcalculator.fixedcost.application.usecase.SetProductionPerMonthCommand
import com.recipecostcalculator.fixedcost.application.usecase.SetProductionPerMonthUseCase
import com.recipecostcalculator.ingredient.FakeIngredientRepository
import com.recipecostcalculator.ingredient.IngredientTestFactory
import com.recipecostcalculator.pricing.FakePricingRepository
import com.recipecostcalculator.recipe.FakeRecipeRepository
import com.recipecostcalculator.recipe.RecipeTestFactory
import com.recipecostcalculator.recipe.domain.model.IngredientComponent
import com.recipecostcalculator.recipe.domain.model.Quantity
import com.recipecostcalculator.recipe.domain.model.UnitOfMeasure
import kotlin.test.Test
import kotlin.test.assertEquals

class CalculatePricingSummaryUseCaseTest {
    private val ingredientRepository = FakeIngredientRepository()
    private val recipeRepository = FakeRecipeRepository()
    private val pricingRepository = FakePricingRepository()
    private val fixedCostRepository = FakeFixedCostRepository()

    private val calculateRecipeCostUseCase = CalculateRecipeCostUseCase(
        recipeRepository = recipeRepository,
        costCalculatorService = CostCalculatorService(ingredientRepository, recipeRepository),
    )
    private val calculateFixedCostSummaryUseCase = CalculateFixedCostSummaryUseCase(fixedCostRepository)
    private val calculatePricingSummaryUseCase = CalculatePricingSummaryUseCase(
        calculateRecipeCostUseCase = calculateRecipeCostUseCase,
        calculateFixedCostSummaryUseCase = calculateFixedCostSummaryUseCase,
        pricingRepository = pricingRepository,
    )

    @Test
    fun `should calculate pricing summary with commission waste and fixed costs`() {
        val flour = ingredientRepository.save(
            IngredientTestFactory.create(
                name = "Harina",
                unit = UnitOfMeasure.GRAM,
                costPerUnit = Money.of("1"),
            ),
        )

        val recipe = recipeRepository.save(
            RecipeTestFactory.create(
                name = "Muzzarella",
                yield = Quantity.of("10", UnitOfMeasure.UNIT),
                components = listOf(
                    IngredientComponent(
                        ingredientId = flour.id,
                        quantity = Quantity.of("1000", UnitOfMeasure.GRAM),
                    ),
                ),
            ),
        )

        AddFixedCostItemUseCase(fixedCostRepository).execute(
            AddFixedCostItemCommand(
                name = "Alquiler",
                monthlyAmount = Money.of("1200"),
            ),
        )
        SetProductionPerMonthUseCase(fixedCostRepository).execute(
            SetProductionPerMonthCommand(pizzasPerMonth = java.math.BigDecimal("12")),
        )

        SavePricingConfigurationUseCase(pricingRepository).execute(
            SavePricingConfigurationCommand(
                sellingPricePerPizza = Money.of("300"),
                commissionPercent = java.math.BigDecimal("10"),
                wastePercent = java.math.BigDecimal("20"),
                targetMarginPercent = java.math.BigDecimal("50"),
            ),
        )

        val summary = calculatePricingSummaryUseCase.execute(CalculatePricingSummaryQuery(recipeId = recipe.id))

        assertEquals("120", summary.variableCostPerPizza.amount.toPlainString())
        assertEquals("100", summary.fixedCostPerPizza.amount.toPlainString())
        assertEquals("220", summary.totalCostPerPizza.amount.toPlainString())
        assertEquals("30", summary.commissionAmount.amount.toPlainString())
        assertEquals("270", summary.netIncome.amount.toPlainString())
        assertEquals("50", summary.grossProfit.amount.toPlainString())
        assertEquals("18.5185", summary.grossMarginPercent.stripTrailingZeros().toPlainString())
        assertEquals("22.7273", summary.markupPercent.stripTrailingZeros().toPlainString())
        assertEquals("440", summary.suggestedPriceForTargetMargin.amount.toPlainString())
        assertEquals("488.888889", summary.suggestedPriceWithCommission.amount.stripTrailingZeros().toPlainString())
    }
}
