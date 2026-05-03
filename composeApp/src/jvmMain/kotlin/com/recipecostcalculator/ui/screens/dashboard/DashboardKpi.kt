package com.recipecostcalculator.ui.screens.dashboard

data class DashboardKpi(
    val variableCostPerPizza: String,
    val variableCostPerLote: String,
    val fixedCostMonthly: String,
    val fixedCostPerPizza: String,
    val totalCostPerPizza: String,
    val sellingPricePerPizza: String,
    val commissionPercent: String,
    val grossProfit: String,
    val grossMarginPercent: String,
    val suggestedPriceTarget: String,
)
