package com.recipecostcalculator.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = androidx.compose.ui.graphics.Color(0xFF4CAF50),
    secondary = androidx.compose.ui.graphics.Color(0xFF8BC34A),
    tertiary = androidx.compose.ui.graphics.Color(0xFFCDDC39),
    background = androidx.compose.ui.graphics.Color(0xFFFAFAFA),
    surface = androidx.compose.ui.graphics.Color(0xFFFFFFFF)
)

@Composable
fun AppTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        content = content
    )
}