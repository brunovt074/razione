package com.recipecostcalculator.domain.model

data class AppSettings(
    val batchSize: Int = 0,
    val wasteFactor: Double = 0.0,
    val discountPct: Double = 0.0,
    val estimatedMonthlyProduction: Int = 0,
    val targetMargin: Double = 0.0,
    val seedVersion: Int = 0
)
