package com.recipecostcalculator.domain.model

import com.recipecostcalculator.financial.domain.model.Percentage

enum class SettingKey(val key: String) {
    BATCH_SIZE("batch_size"),
    WASTE_FACTOR("waste_factor"),
    COMMISSION_PCT("commission_pct"),
    ESTIMATED_MONTHLY_PRODUCTION("est_monthly_prod"),
    TARGET_MARGIN("target_margin"),
    DEFAULT_COST_MODE("default_cost_mode")
}

data class AppSettings(
    val batchSize: Int = 6,
    val wasteFactor: Percentage = Percentage.ZERO,
    val commissionPct: Percentage = Percentage.ZERO,
    val estimatedMonthlyProduction: Int = 0,
    val targetMargin: Percentage = Percentage.fromPercent(50.0),
    val defaultCostMode: CostMode = CostMode.VARIABLE
) {
    init {
        require(batchSize > 0) { "Batch size must be positive" }
        require(estimatedMonthlyProduction >= 0) { "Estimated monthly production cannot be negative" }
    }
}