package com.recipecostcalculator.ui.components.input

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.recipecostcalculator.ui.theme.RazioneRadii
import com.recipecostcalculator.ui.theme.RazioneSpacing
import com.recipecostcalculator.ui.theme.RazioneTheme

@Composable
private fun StepButton(symbol: String, onClick: () -> Unit) {
    val colors = RazioneTheme.colors
    Box(
        modifier = Modifier
            .size(RazioneSpacing.minTouchTarget)
            .background(colors.surfaceRaised, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(symbol, style = RazioneTheme.typography.section, color = colors.textPrimary)
    }
}

@Composable
fun QuantityUnitField(
    quantity: String,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    unit: RazioneUnit,
    onUnitChange: (RazioneUnit) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = RazioneTheme.colors
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(RazioneSpacing.l)
    ) {
        StepButton("−", onDecrease)
        Text(
            text = quantity,
            style = RazioneTheme.typography.figure,
            color = colors.textPrimary
        )
        StepButton("+", onIncrease)

        Row(horizontalArrangement = Arrangement.spacedBy(RazioneSpacing.xs)) {
            RazioneUnit.entries.forEach { candidate ->
                val selected = candidate == unit
                Box(
                    modifier = Modifier
                        .background(
                            if (selected) colors.accent else colors.surfaceRaised,
                            RoundedCornerShape(RazioneRadii.pill)
                        )
                        .clickable { onUnitChange(candidate) }
                        .padding(horizontal = RazioneSpacing.m, vertical = RazioneSpacing.s),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = candidate.label,
                        style = RazioneTheme.typography.label,
                        color = if (selected) colors.background else colors.textSecondary
                    )
                }
            }
        }
    }
}
