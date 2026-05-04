package com.recipecostcalculator.pricing.domain.service

import com.recipecostcalculator.domain.model.CostBreakdown
import com.recipecostcalculator.financial.domain.model.Money
import com.recipecostcalculator.financial.domain.model.Percentage
import com.recipecostcalculator.pricing.domain.model.PriceAnalysis
import com.recipecostcalculator.pricing.domain.model.SuggestedPrice
import com.recipecostcalculator.domain.model.CostMode
import java.math.BigDecimal

class PricingCalculator {

    fun calculateSuggestedPrice(
        variableCost: Money,
        targetMargin: Percentage,
        commissionPct: Percentage,
    ): SuggestedPrice {
        val withoutCommission = if (targetMargin.complementIsZero()) {
            Money.ZERO
        } else {
            variableCost / targetMargin.complement().toDouble()
        }

        val withCommission = if (commissionPct.complementIsZero()) {
            Money.ZERO
        } else {
            withoutCommission / commissionPct.complement().toDouble()
        }

        return SuggestedPrice(withoutCommission, withCommission)
    }

    fun analyze(
        breakdown: CostBreakdown,
        salePrice: Money,
        commissionPct: Percentage,
        targetMargin: Percentage,
        costMode: CostMode,
    ): PriceAnalysis {
        val commissionAmount = salePrice * commissionPct.toDouble()
        val netIncome = salePrice - commissionAmount
        val chosenCost = when (costMode) {
            CostMode.VARIABLE -> breakdown.totalVariableCost
            CostMode.TOTAL -> breakdown.totalCostPerUnit
            else -> breakdown.totalVariableCost
        }

        val grossProfit = netIncome - breakdown.totalVariableCost

        val grossMargin: Percentage? = if (netIncome.isZero()) null else {
            runCatching {
                Percentage.fromDecimal(
                    grossProfit.amount.divide(netIncome.amount, 4, java.math.RoundingMode.HALF_UP)
                )
            }.getOrNull()
        }

        val markup: Double? = if (breakdown.totalVariableCost.isZero()) null else {
            grossProfit.amount.divide(breakdown.totalVariableCost.amount, 4, java.math.RoundingMode.HALF_UP).toDouble()
        }

        val suggestedPrice = calculateSuggestedPrice(
            breakdown.totalVariableCost, targetMargin, commissionPct
        )

        return PriceAnalysis(
            recipeId = breakdown.recipeId,
            recipeName = breakdown.recipeName,
            salePrice = salePrice,
            commissionAmount = commissionAmount,
            netIncome = netIncome,
            variableCost = breakdown.totalVariableCost,
            grossProfit = grossProfit,
            grossMargin = grossMargin,
            markup = markup,
            suggestedPrice = suggestedPrice,
            costMode = costMode,
            chosenCost = chosenCost,
        )
    }
}