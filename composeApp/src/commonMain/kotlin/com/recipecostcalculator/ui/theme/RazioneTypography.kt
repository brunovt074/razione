package com.recipecostcalculator.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

data class RazioneTypography(
    val display: TextStyle,
    val title: TextStyle,
    val section: TextStyle,
    val body: TextStyle,
    val caption: TextStyle,
    val label: TextStyle,
    val figure: TextStyle
)

private val negativeTracking: TextUnit = (-0.02).em

@Composable
fun razioneTypography(): RazioneTypography {
    val interfaceFont = razioneInterfaceFontFamily()
    val figureFont = razioneFigureFontFamily()
    return RazioneTypography(
        display = TextStyle(
            fontFamily = interfaceFont,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 44.sp,
            letterSpacing = negativeTracking
        ),
        title = TextStyle(
            fontFamily = interfaceFont,
            fontWeight = FontWeight.Bold,
            fontSize = 30.sp,
            letterSpacing = negativeTracking
        ),
        section = TextStyle(
            fontFamily = interfaceFont,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        ),
        body = TextStyle(
            fontFamily = interfaceFont,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp
        ),
        caption = TextStyle(
            fontFamily = interfaceFont,
            fontWeight = FontWeight.Normal,
            fontSize = 13.sp
        ),
        label = TextStyle(
            fontFamily = figureFont,
            fontWeight = FontWeight.Normal,
            fontSize = 10.sp
        ),
        figure = TextStyle(
            fontFamily = figureFont,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            fontFeatureSettings = "tnum"
        )
    )
}
