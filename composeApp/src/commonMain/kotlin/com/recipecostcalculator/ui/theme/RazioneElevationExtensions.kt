package com.recipecostcalculator.ui.theme

import androidx.compose.ui.graphics.Color

fun RazioneColors.surfaceFor(level: RazioneElevationLevel): Color = when (level) {
    RazioneElevationLevel.Background -> background
    RazioneElevationLevel.Card -> surface
    RazioneElevationLevel.Field -> surfaceRaised
    RazioneElevationLevel.ModalSheet -> surfaceRaised
}

fun RazioneColors.borderFor(level: RazioneElevationLevel): Color? = when (level) {
    RazioneElevationLevel.Card -> border
    else -> null
}
