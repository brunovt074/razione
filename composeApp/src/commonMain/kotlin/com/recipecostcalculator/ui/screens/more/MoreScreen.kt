package com.recipecostcalculator.ui.screens.more

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.recipecostcalculator.ui.strings.es.More

@Composable
fun MoreScreen(
    onNavigateToFixedCosts: () -> Unit,
    onNavigateToConfiguration: () -> Unit,
    onNavigateToAdditionalCosts: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = More.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        MenuCard(
            title = More.fixedCosts,
            subtitle = More.fixedCostsSubtitle,
            onClick = onNavigateToFixedCosts
        )

        MenuCard(
            title = More.supplyCosts,
            subtitle = More.supplyCostsSubtitle,
            onClick = onNavigateToAdditionalCosts
        )

        MenuCard(
            title = More.configuration,
            subtitle = More.configurationSubtitle,
            onClick = onNavigateToConfiguration
        )
    }
}

@Composable
private fun MenuCard(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}