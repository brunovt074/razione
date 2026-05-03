package com.recipecostcalculator.ui.screens.fixedcosts

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
import com.recipecostcalculator.domain.model.FixedCost
import com.recipecostcalculator.ui.viewmodel.FixedCostsViewModel
import com.recipecostcalculator.ui.strings.es.Common
import com.recipecostcalculator.ui.strings.es.FixedCosts

@Suppress("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FixedCostsScreen(
    viewModel: FixedCostsViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showBottomSheet by remember { mutableStateOf(false) }
    var editingCost by remember { mutableStateOf<FixedCost?>(null) }
    var costToDelete by remember { mutableStateOf<FixedCost?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(FixedCosts.title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = Common.back)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { editingCost = null; showBottomSheet = true }) {
                Text("+")
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
                        FixedCostCard(
                            fixedCost = cost,
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
                            text = FixedCosts.totalMonthly,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$${String.format("%.2f", state.totalMonthly)}",
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
            FixedCostForm(
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
            title = { Text(FixedCosts.deleteCost) },
            text = { Text(FixedCosts.deleteCostConfirmation.format(cost.concept)) },
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
private fun FixedCostCard(
    fixedCost: FixedCost,
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
                text = fixedCost.concept,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "$${String.format("%.2f", fixedCost.monthlyAmount)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun FixedCostForm(
    cost: FixedCost?,
    onSave: (FixedCost) -> Unit,
    onCancel: () -> Unit
) {
    var concept by remember { mutableStateOf(cost?.concept ?: "") }
    var amount by remember { mutableStateOf(cost?.monthlyAmount?.toString() ?: "") }
    var conceptError by remember { mutableStateOf(false) }
    var amountError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = if (cost == null) FixedCosts.newFixedCost else FixedCosts.editFixedCost,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        OutlinedTextField(
            value = concept,
            onValueChange = { concept = it.take(50); conceptError = false },
            label = { Text(FixedCosts.concept) },
            modifier = Modifier.fillMaxWidth(),
            isError = conceptError,
            supportingText = if (conceptError) {{ Text(Common.required) }} else null
        )

        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it; amountError = false },
            label = { Text(FixedCosts.monthlyAmount) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            prefix = { Text("$") },
            isError = amountError,
            supportingText = if (amountError) {{ Text(Common.required) }} else null
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
                    amountError = amount.toDoubleOrNull() == null
                    if (!conceptError && !amountError) {
                        val newCost = FixedCost(
                            id = cost?.id ?: 0,
                            concept = concept.trim(),
                            monthlyAmount = amount.toDouble()
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