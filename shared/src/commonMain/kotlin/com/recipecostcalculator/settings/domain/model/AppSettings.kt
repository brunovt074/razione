package com.recipecostcalculator.settings.domain.model

import com.recipecostcalculator.financial.domain.model.Percentage
import com.recipecostcalculator.pricing.domain.model.CostMode

data class AppSettings(
    val wasteFactor: Percentage = Percentage.ZERO,
    val batchSize: Int = 6,
    val commissionPct: Percentage = Percentage.ZERO,
    val targetMargin: Percentage = Percentage(0.5),
    val estimatedMonthlyProduction: Int = 0,
    val defaultCostMode: CostMode = CostMode.VARIABLE,
) {
    init {
        require(batchSize > 0) { "Batch size must be positive" }
        require(estimatedMonthlyProduction >= 0) {
            "Estimated monthly production cannot be negative"
        }
    }
}