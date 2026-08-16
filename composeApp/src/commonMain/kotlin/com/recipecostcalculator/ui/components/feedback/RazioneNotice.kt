package com.recipecostcalculator.ui.components.feedback

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.recipecostcalculator.ui.components.icon.RazioneIcons
import com.recipecostcalculator.ui.theme.RazioneRadii
import com.recipecostcalculator.ui.theme.RazioneSpacing
import com.recipecostcalculator.ui.theme.RazioneTheme

@Composable
fun RazioneNotice(
    title: String,
    detail: String,
    modifier: Modifier = Modifier
) {
    val colors = RazioneTheme.colors
    Row(
        modifier = modifier
            .background(colors.surfaceRaised, RoundedCornerShape(RazioneRadii.control))
            .padding(RazioneSpacing.l),
        horizontalArrangement = Arrangement.spacedBy(RazioneSpacing.m)
    ) {
        Icon(
            imageVector = RazioneIcons.alert,
            contentDescription = null,
            tint = colors.tight,
            modifier = Modifier.size(24.dp)
        )
        Column(verticalArrangement = Arrangement.spacedBy(RazioneSpacing.xs)) {
            Text(text = title, style = RazioneTheme.typography.body, color = colors.textPrimary)
            Text(text = detail, style = RazioneTheme.typography.caption, color = colors.textSecondary)
        }
    }
}
