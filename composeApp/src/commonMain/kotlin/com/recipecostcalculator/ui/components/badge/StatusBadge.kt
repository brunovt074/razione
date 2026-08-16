package com.recipecostcalculator.ui.components.badge

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.recipecostcalculator.ui.theme.RazioneColors
import com.recipecostcalculator.ui.theme.RazioneRadii
import com.recipecostcalculator.ui.theme.RazioneSpacing
import com.recipecostcalculator.ui.theme.RazioneTheme

private fun RazioneColors.colorFor(status: RazioneStatus): Color = when (status) {
    RazioneStatus.Profitable -> profitable
    RazioneStatus.Tight -> tight
    RazioneStatus.AtLoss -> atLoss
    RazioneStatus.Estimated -> data
}

@Composable
fun StatusBadge(
    status: RazioneStatus,
    label: String,
    modifier: Modifier = Modifier,
    percentage: String? = null
) {
    val tone = RazioneTheme.colors.colorFor(status)
    Row(
        modifier = modifier
            .background(tone.copy(alpha = 0.16f), RoundedCornerShape(RazioneRadii.badge))
            .padding(horizontal = RazioneSpacing.s, vertical = RazioneSpacing.xs),
        horizontalArrangement = Arrangement.spacedBy(RazioneSpacing.xs)
    ) {
        Text(text = label, style = RazioneTheme.typography.caption, color = tone)
        if (percentage != null) {
            Text(text = percentage, style = RazioneTheme.typography.label, color = tone)
        }
    }
}
