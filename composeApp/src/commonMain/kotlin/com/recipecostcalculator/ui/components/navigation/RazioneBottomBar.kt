package com.recipecostcalculator.ui.components.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.recipecostcalculator.ui.theme.RazioneSpacing
import com.recipecostcalculator.ui.theme.RazioneTheme

@Composable
fun RazioneBottomBar(
    destinations: List<RazioneNavDestination>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    require(destinations.size <= 4) { "RazioneBottomBar admite hasta 4 destinos" }
    val colors = RazioneTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.surface)
            .padding(vertical = RazioneSpacing.s),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        destinations.forEachIndexed { index, destination ->
            val selected = index == selectedIndex
            val tint = if (selected) colors.figureHighlight else colors.textSecondary
            Column(
                modifier = Modifier
                    .clickable { onSelect(index) }
                    .padding(RazioneSpacing.s),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(RazioneSpacing.xs)
            ) {
                Icon(
                    imageVector = destination.icon,
                    contentDescription = destination.label,
                    tint = tint,
                    modifier = Modifier.size(24.dp)
                )
                Text(text = destination.label, style = RazioneTheme.typography.label, color = tint)
            }
        }
    }
}
