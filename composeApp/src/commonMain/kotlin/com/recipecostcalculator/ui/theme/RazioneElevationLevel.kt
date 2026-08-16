package com.recipecostcalculator.ui.theme

enum class RazioneElevationLevel(val hasShadow: Boolean) {
    Background(hasShadow = false),
    Card(hasShadow = false),
    Field(hasShadow = false),
    ModalSheet(hasShadow = true)
}
