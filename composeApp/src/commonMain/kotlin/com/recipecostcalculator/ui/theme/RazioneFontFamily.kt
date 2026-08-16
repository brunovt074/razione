package com.recipecostcalculator.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import recipe_cost_calculator.composeapp.generated.resources.Res
import recipe_cost_calculator.composeapp.generated.resources.manrope_variable
import recipe_cost_calculator.composeapp.generated.resources.martian_mono_variable
import org.jetbrains.compose.resources.Font

@Composable
fun razioneInterfaceFontFamily(): FontFamily = FontFamily(
    Font(Res.font.manrope_variable, FontWeight.Light),
    Font(Res.font.manrope_variable, FontWeight.Normal),
    Font(Res.font.manrope_variable, FontWeight.Medium),
    Font(Res.font.manrope_variable, FontWeight.SemiBold),
    Font(Res.font.manrope_variable, FontWeight.Bold),
    Font(Res.font.manrope_variable, FontWeight.ExtraBold)
)

@Composable
fun razioneFigureFontFamily(): FontFamily = FontFamily(
    Font(Res.font.martian_mono_variable, FontWeight.Normal),
    Font(Res.font.martian_mono_variable, FontWeight.Medium),
    Font(Res.font.martian_mono_variable, FontWeight.Bold)
)
