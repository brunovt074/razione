package com.recipecostcalculator.domain.model
// SEPARAR
enum class SettingKey(val key: String) {
    BATCH_SIZE("batch_size"),
    WASTE_FACTOR("waste_factor"),
    DISCOUNT_PCT("discount_pct"),
    ESTIMATED_MONTHLY_PRODUCTION("est_monthly_prod"),
    TARGET_MARGIN("target_margin"),
    SEED_VERSION("seed_version")
}
// REFACTOR 
data class AppSettings(
    val batchSize: Int = 0,
    val wasteFactor: Double = 0.0,
    val discountPct: Double = 0.0,
    val estimatedMonthlyProduction: Int = 0,
    val targetMargin: Double = 0.0,
    val seedVersion: Int = 0
)
