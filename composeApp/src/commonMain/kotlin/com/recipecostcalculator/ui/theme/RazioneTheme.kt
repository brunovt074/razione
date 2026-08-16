package com.recipecostcalculator.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

object RazioneTheme {
    val colors: RazioneColors
        @Composable get() = LocalRazioneColors.current

    val typography: RazioneTypography
        @Composable get() = razioneTypography()

    val spacing = RazioneSpacing
    val radii = RazioneRadii
}

@Composable
fun RazioneTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val roles = if (darkTheme) DarkColorRoles else LightColorRoles
    val materialColorScheme = if (darkTheme) {
        darkColorScheme(
            background = roles.background,
            surface = roles.surface,
            surfaceVariant = roles.surfaceRaised,
            outline = roles.border,
            primary = roles.accent,
            onBackground = roles.textPrimary,
            onSurface = roles.textPrimary,
            onPrimary = roles.background,
            error = roles.atLoss
        )
    } else {
        lightColorScheme(
            background = roles.background,
            surface = roles.surface,
            surfaceVariant = roles.surfaceRaised,
            outline = roles.border,
            primary = roles.accent,
            onBackground = roles.textPrimary,
            onSurface = roles.textPrimary,
            onPrimary = roles.background,
            error = roles.atLoss
        )
    }

    CompositionLocalProvider(LocalRazioneColors provides roles) {
        MaterialTheme(
            colorScheme = materialColorScheme,
            content = content
        )
    }
}
