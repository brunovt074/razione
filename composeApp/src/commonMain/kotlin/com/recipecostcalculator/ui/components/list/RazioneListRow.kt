package com.recipecostcalculator.ui.components.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.recipecostcalculator.ui.theme.RazioneSpacing
import com.recipecostcalculator.ui.theme.RazioneTheme

@Composable
fun RazioneListRow(
    name: String,
    figure: String,
    modifier: Modifier = Modifier,
    detail: String? = null
) {
    val colors = RazioneTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = RazioneSpacing.s),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = name, style = RazioneTheme.typography.body, color = colors.textPrimary)
            if (detail != null) {
                Text(text = detail, style = RazioneTheme.typography.caption, color = colors.textSecondary)
            }
        }
        Text(text = figure, style = RazioneTheme.typography.figure, color = colors.textPrimary)
    }
}
