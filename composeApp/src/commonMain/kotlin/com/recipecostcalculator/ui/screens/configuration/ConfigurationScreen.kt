package com.recipecostcalculator.ui.screens.configuration

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.recipecostcalculator.ui.viewmodel.SettingsViewModel
import com.recipecostcalculator.ui.strings.es.Configuration

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigurationScreen(
    settingsViewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val settings by settingsViewModel.settings.collectAsStateWithLifecycle()
    var batchSizeText by remember(settings.batchSize) { mutableStateOf(settings.batchSize.toString()) }
    var estimatedProductionText by remember(settings.estimatedMonthlyProduction) { mutableStateOf(settings.estimatedMonthlyProduction.toString()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(Configuration.title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("←")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = Configuration.production,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = batchSizeText,
                        onValueChange = { newValue ->
                            batchSizeText = newValue
                            newValue.toIntOrNull()?.let { value ->
                                if (value in 1..100) {
                                    settingsViewModel.updateBatchSize(value)
                                }
                            }
                        },
                        label = { Text(Configuration.batchSize) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    OutlinedTextField(
                        value = estimatedProductionText,
                        onValueChange = { newValue ->
                            estimatedProductionText = newValue
                            newValue.toIntOrNull()?.let { value ->
                                if (value >= 0) {
                                    settingsViewModel.updateEstimatedProduction(value)
                                }
                            }
                        },
                        label = { Text(Configuration.estimatedMonthlyProduction) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = Configuration.costs,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text("${Configuration.waste}: ${(settings.wasteFactor * 100).toInt()}%")
                    Slider(
                        value = settings.wasteFactor.toFloat(),
                        onValueChange = { settingsViewModel.updateWasteFactor(it.toDouble()) },
                        valueRange = 0f..0.5f
                    )

                    Text("${Configuration.discount}: ${(settings.discountPct * 100).toInt()}%")
                    Slider(
                        value = settings.discountPct.toFloat(),
                        onValueChange = { settingsViewModel.updateDiscountPct(it.toDouble()) },
                        valueRange = 0f..0.5f
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = Configuration.sales,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text("${Configuration.targetMargin}: ${(settings.targetMargin * 100).toInt()}%")
                    Slider(
                        value = settings.targetMargin.toFloat(),
                        onValueChange = { settingsViewModel.updateTargetMargin(it.toDouble()) },
                        valueRange = 0f..1f
                    )
                }
            }
        }
    }
}