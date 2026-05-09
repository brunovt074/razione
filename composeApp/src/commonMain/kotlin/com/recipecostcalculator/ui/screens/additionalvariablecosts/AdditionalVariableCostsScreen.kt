package com.recipecostcalculator.ui.screens.additionalvariablecosts

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.recipecostcalculator.domain.model.AdditionalVariableCost
import com.recipecostcalculator.ui.viewmodel.AdditionalVariableCostsViewModel
import com.recipecostcalculator.ui.strings.es.AdditionalVariableCosts
import com.recipecostcalculator.ui.strings.es.Common

@Suppress("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdditionalVariableCostsScreen(
    viewModel: AdditionalVariableCostsViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showBottomSheet by remember { mutableStateOf(false) }
    var editingCost by remember { mutableStateOf<AdditionalVariableCost?>(null) }
    var costToDelete by remember { mutableStateOf<AdditionalVariableCost?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(Unit) {
        viewModel.loadCosts()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(AdditionalVariableCosts.title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = Common.back)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { editingCost = null; showBottomSheet = true }) {
                Text(Common.addButton)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (state.isLoading) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.costs) { cost ->
                        AdditionalVariableCostCard(
                            cost = cost,
                            onClick = { editingCost = cost; showBottomSheet = true },
                            onLongClick = { costToDelete = cost }
                        )
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = AdditionalVariableCosts.total,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$${String.format("%.2f", state.totalCost.amount)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false; editingCost = null },
            sheetState = sheetState
        ) {
            AdditionalVariableCostForm(
                cost = editingCost,
                onSave = { cost ->
                    viewModel.onSave(cost)
                    showBottomSheet = false
                    editingCost = null
                },
                onCancel = { showBottomSheet = false; editingCost = null }
            )
        }
    }

    costToDelete?.let { cost ->
        AlertDialog(
            onDismissRequest = { costToDelete = null },
            title = { Text(AdditionalVariableCosts.deleteCost) },
            text = { Text(AdditionalVariableCosts.deleteCostConfirmation.format(cost.concept)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.onDelete(cost.id)
                    costToDelete = null
                }) {
                    Text(Common.delete)
                }
            },
            dismissButton = {
                TextButton(onClick = { costToDelete = null }) {
                    Text(Common.cancel)
                }
            }
        )
    }
}

@Suppress("DefaultLocale")
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AdditionalVariableCostCard(
    cost: AdditionalVariableCost,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = cost.concept,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "$${String.format("%.2f", cost.unitCost.amount)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            if (cost.note.isNotBlank()) {
                Text(
                    text = cost.note,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun AdditionalVariableCostForm(
    cost: AdditionalVariableCost?,
    onSave: (AdditionalVariableCost) -> Unit,
    onCancel: () -> Unit
) {
    var concept by remember { mutableStateOf(cost?.concept ?: "") }
    var unitCost by remember { mutableStateOf(cost?.unitCost?.toString() ?: "") }
    var note by remember { mutableStateOf(cost?.note ?: "") }
    var conceptError by remember { mutableStateOf(false) }
    var unitCostError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = if (cost == null) AdditionalVariableCosts.newCost else AdditionalVariableCosts.editCost,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        OutlinedTextField(
            value = concept,
            onValueChange = { concept = it.take(50); conceptError = false },
            label = { Text(AdditionalVariableCosts.concept) },
            modifier = Modifier.fillMaxWidth(),
            isError = conceptError,
            supportingText = if (conceptError) {{ Text(Common.required) }} else null
        )

        OutlinedTextField(
            value = unitCost,
            onValueChange = { unitCost = it; unitCostError = false },
            label = { Text(AdditionalVariableCosts.unitCost) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            prefix = { Text("$") },
            isError = unitCostError,
            supportingText = if (unitCostError) {{ Text(Common.required) }} else null
        )

        OutlinedTextField(
            value = note,
            onValueChange = { note = it.take(100) },
            label = { Text(AdditionalVariableCosts.note) },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 2
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f)
            ) {
                Text(Common.cancel)
            }
            Button(
                onClick = {
                    conceptError = concept.isBlank()
                    unitCostError = unitCost.toDoubleOrNull() == null
                    if (!conceptError && !unitCostError) {
                        val newCost = AdditionalVariableCost(
                            id = cost?.id ?: 0,
                            recipeId = cost?.recipeId,
                            concept = concept.trim(),
                            unitCost = com.recipecostcalculator.financial.domain.model.Money(unitCost.toDouble()),
                            note = note.trim()
                        )
                        onSave(newCost)
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(Common.save)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
