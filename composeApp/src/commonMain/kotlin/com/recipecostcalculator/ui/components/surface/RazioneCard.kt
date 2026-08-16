package com.recipecostcalculator.ui.components.surface

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.recipecostcalculator.ui.theme.RazioneRadii
import com.recipecostcalculator.ui.theme.RazioneSpacing
import com.recipecostcalculator.ui.theme.RazioneTheme

@Composable
fun RazioneCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = RazioneTheme.colors
    val shape = RoundedCornerShape(RazioneRadii.card)
    Column(
        modifier = modifier
            .background(colors.surface, shape)
            .border(BorderStroke(1.dp, colors.border), shape)
            .padding(RazioneSpacing.xl),
        content = content
    )
}
