package com.recipecostcalculator.pricing.application.usecase

import com.recipecostcalculator.costing.application.usecase.CalculateRecipeCostQuery
import com.recipecostcalculator.costing.application.usecase.CalculateRecipeCostUseCase
import com.recipecostcalculator.financial.domain.model.Money
import com.recipecostcalculator.fixedcost.application.usecase.CalculateFixedCostSummaryUseCase
import com.recipecostcalculator.pricing.application.dto.PricingSummaryDto
import com.recipecostcalculator.pricing.domain.repository.PricingRepository
import java.math.BigDecimal
import java.math.RoundingMode

class CalculatePricingSummaryUseCase(
    private val calculateRecipeCostUseCase: CalculateRecipeCostUseCase,
    private val calculateFixedCostSummaryUseCase: CalculateFixedCostSummaryUseCase,
    private val pricingRepository: PricingRepository,
) {
    fun execute(query: CalculatePricingSummaryQuery): PricingSummaryDto {
        val variableCostResult = calculateRecipeCostUseCase.execute(CalculateRecipeCostQuery(recipeId = query.recipeId))
        val fixedSummary = calculateFixedCostSummaryUseCase.execute()
        val pricingConfiguration = pricingRepository.getPricingConfiguration()
        val commissionRate = pricingConfiguration.commissionPercent.divide(BigDecimal("100"), 6, RoundingMode.HALF_UP)
        val marginRate = pricingConfiguration.targetMarginPercent.divide(BigDecimal("100"), 6, RoundingMode.HALF_UP)

        require(commissionRate < BigDecimal.ONE) { "La comision/descuento debe ser menor a 100" }
        require(marginRate < BigDecimal.ONE) { "El margen objetivo debe ser menor a 100" }

        val wasteFactor = BigDecimal.ONE.add(
            pricingConfiguration.wastePercent.divide(BigDecimal("100"), 6, RoundingMode.HALF_UP),
        )
        val variableCostPerPizza = variableCostResult.costPerUnit * wasteFactor
        val fixedCostPerPizza = fixedSummary.fixedCostPerPizza
        val totalCostPerPizza = variableCostPerPizza + fixedCostPerPizza

        val commissionAmount = pricingConfiguration.sellingPricePerPizza * commissionRate
        val netIncome = pricingConfiguration.sellingPricePerPizza - commissionAmount
        val grossProfit = netIncome - totalCostPerPizza

        val grossMarginPercent = if (netIncome.amount.signum() == 0) {
            BigDecimal.ZERO
        } else {
            grossProfit.amount
                .divide(netIncome.amount, 6, RoundingMode.HALF_UP)
                .multiply(BigDecimal("100"))
        }

        val markupPercent = if (totalCostPerPizza.amount.signum() == 0) {
            BigDecimal.ZERO
        } else {
            grossProfit.amount
                .divide(totalCostPerPizza.amount, 6, RoundingMode.HALF_UP)
                .multiply(BigDecimal("100"))
        }

        val suggestedPriceForMargin = Money.of(
            totalCostPerPizza.amount.divide(BigDecimal.ONE.subtract(marginRate), 6, RoundingMode.HALF_UP),
            totalCostPerPizza.currency,
        )

        val suggestedPriceWithCommission = Money.of(
            suggestedPriceForMargin.amount.divide(BigDecimal.ONE.subtract(commissionRate), 6, RoundingMode.HALF_UP),
            suggestedPriceForMargin.currency,
        )

        return PricingSummaryDto(
            variableCostPerPizza = variableCostPerPizza,
            fixedCostPerPizza = fixedCostPerPizza,
            totalCostPerPizza = totalCostPerPizza,
            pricingConfiguration = pricingConfiguration,
            commissionAmount = commissionAmount,
            netIncome = netIncome,
            grossProfit = grossProfit,
            grossMarginPercent = grossMarginPercent,
            markupPercent = markupPercent,
            suggestedPriceForTargetMargin = suggestedPriceForMargin,
            suggestedPriceWithCommission = suggestedPriceWithCommission,
        )
    }
}
