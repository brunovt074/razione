package com.recipecostcalculator.domain.model

enum class SettingKey(val key: String) {
    BATCH_SIZE("batch_size"),
    WASTE_FACTOR("waste_factor"),
    DISCOUNT_PCT("discount_pct"),
    ESTIMATED_MONTHLY_PRODUCTION("est_monthly_prod"),
    TARGET_MARGIN("target_margin"),
    SEED_VERSION("seed_version")
}
