package com.recipecostcalculator.pricing.domain.service

import com.recipecostcalculator.costing.domain.model.CostBreakdown
import com.recipecostcalculator.financial.domain.model.Money
import com.recipecostcalculator.financial.domain.model.Percentage
import com.recipecostcalculator.pricing.domain.model.CostMode
import com.recipecostcalculator.pricing.domain.model.PriceAnalysis
import com.recipecostcalculator.pricing.domain.model.SuggestedPrice

class PricingCalculator {
    fun calculateSuggestedPrice(
        variableCost: Money,
        targetMargin: Percentage,
        commissionPct: Percentage,
    ): SuggestedPrice {
        val withoutCommission = if (targetMargin.complementIsZero()) {
            Money.ZERO
        } else {
            variableCost / targetMargin.complement()
        }

        val withCommission = if (commissionPct.complementIsZero()) {
            Money.ZERO
        } else {
            withoutCommission / commissionPct.complement()
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
        }

        val grossProfit = netIncome - breakdown.totalVariableCost

        val grossMargin: Percentage? = if (netIncome.isZero()) null else {
            runCatching {
                Percentage(grossProfit.amount / netIncome.amount)
            }.getOrNull()
        }

        val markup: Double? = if (breakdown.totalVariableCost.isZero()) null else {
            grossProfit.amount / breakdown.totalVariableCost.amount
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