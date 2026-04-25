package com.recipecostcalculator.ui.components

import java.math.BigDecimal

fun parseDecimalOrNull(raw: String): BigDecimal? {
    val normalized = raw.trim().replace(',', '.')
    if (normalized.isBlank()) {
        return null
    }
    return normalized.toBigDecimalOrNull()
}
