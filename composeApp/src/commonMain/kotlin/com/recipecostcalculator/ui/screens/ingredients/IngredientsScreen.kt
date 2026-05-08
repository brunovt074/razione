package com.recipecostcalculator.ui.screens.ingredients

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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.recipecostcalculator.domain.model.Ingredient
import com.recipecostcalculator.ui.viewmodel.IngredientsViewModel
import com.recipecostcalculator.ui.strings.es.Common
import com.recipecostcalculator.ui.strings.es.Ingredients

private val purchaseUnits = listOf("kg", "lt", "un")
private val usageUnits = listOf("kg", "g", "lt", "cc", "un")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngredientsScreen(viewModel: IngredientsViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showBottomSheet by remember { mutableStateOf(false) }
    var editingIngredient by remember { mutableStateOf<Ingredient?>(null) }
    var priceDialogIngredient by remember { mutableStateOf<Ingredient?>(null) }
    var deleteDialogIngredient by remember { mutableStateOf<Ingredient?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(Unit) {
        viewModel.loadIngredients()
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { editingIngredient = null; showBottomSheet = true }) {
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
            Text(
                text = Ingredients.title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            if (state.isLoading) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                }
            } else if (state.ingredients.isEmpty()) {
                Text(
                    text = Ingredients.noIngredients,
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.ingredients) { ingredient ->
                        IngredientCard(
                            ingredient = ingredient,
                            onPriceClick = { priceDialogIngredient = ingredient },
                            onCardClick = { editingIngredient = ingredient; showBottomSheet = true },
                            onLongClick = { deleteDialogIngredient = ingredient }
                        )
                    }
                }
            }
        }
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false; editingIngredient = null },
            sheetState = sheetState
        ) {
            IngredientForm(
                ingredient = editingIngredient,
                onSave = { ingredient ->
                    viewModel.onSave(ingredient)
                    showBottomSheet = false
                    editingIngredient = null
                },
                onCancel = { showBottomSheet = false; editingIngredient = null }
            )
        }
    }

    priceDialogIngredient?.let { ingredient ->
        PriceUpdateDialog(
            currentPrice = ingredient.purchasePrice,
            onDismiss = { priceDialogIngredient = null },
            onConfirm = { newPrice ->
                viewModel.onUpdatePrice(ingredient.id, newPrice)
                priceDialogIngredient = null
            }
        )
    }

    deleteDialogIngredient?.let { ingredient ->
        AlertDialog(
            onDismissRequest = { deleteDialogIngredient = null },
            title = { Text(Ingredients.deleteIngredient) },
            text = { Text(Ingredients.deleteIngredientConfirmation.format(ingredient.name)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.onDelete(ingredient.id)
                    deleteDialogIngredient = null
                }) {
                    Text(Common.delete)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteDialogIngredient = null }) {
                    Text(Common.cancel)
                }
            }
        )
    }
}

@Suppress("DefaultLocale")
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun IngredientCard(
    ingredient: Ingredient,
    onPriceClick: () -> Unit,
    onCardClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onCardClick,
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
                text = ingredient.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${ingredient.contentAmount} ${ingredient.purchaseUnit}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "$${String.format("%.2f", ingredient.purchasePrice)} / ${ingredient.purchaseUnit}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.combinedClickable(
                    onClick = onPriceClick,
                    onLongClick = onLongClick
                )
            )
        }
    }
}

@Composable
private fun PriceUpdateDialog(
    currentPrice: Double,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var priceText by remember { mutableStateOf(currentPrice.toString()) }
    var error by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(Ingredients.updatePrice) },
        text = {
            OutlinedTextField(
                value = priceText,
                onValueChange = { priceText = it; error = false },
                label = { Text(Ingredients.price) },
                prefix = { Text("$") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = error,
                supportingText = if (error) {{ Text(Ingredients.enterValidNumber) }} else null
            )
        },
        confirmButton = {
            TextButton(onClick = {
                val newPrice = priceText.toDoubleOrNull()
                if (newPrice != null && newPrice > 0) {
                    onConfirm(newPrice)
                } else {
                    error = true
                }
            }) {
                Text(Common.accept)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(Common.cancel)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IngredientForm(
    ingredient: Ingredient?,
    onSave: (Ingredient) -> Unit,
    onCancel: () -> Unit
) {
    var name by remember { mutableStateOf(ingredient?.name ?: "") }
    var purchaseUnit by remember { mutableStateOf(ingredient?.purchaseUnit ?: "kg") }
    var purchasePrice by remember { mutableStateOf(ingredient?.purchasePrice?.toString() ?: "") }
    var contentAmount by remember { mutableStateOf(ingredient?.contentAmount?.toString() ?: "") }
    var usageUnit by remember { mutableStateOf(ingredient?.usageUnit ?: "g") }

    var nameError by remember { mutableStateOf(false) }
    var purchasePriceError by remember { mutableStateOf(false) }
    var contentAmountError by remember { mutableStateOf(false) }
    var purchaseUnitExpanded by remember { mutableStateOf(false) }
    var usageUnitExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = if (ingredient == null) Ingredients.newIngredient else Ingredients.editIngredient,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        OutlinedTextField(
            value = name,
            onValueChange = { name = it.take(50); nameError = false },
            label = { Text(Ingredients.name) },
            modifier = Modifier.fillMaxWidth(),
            isError = nameError,
            supportingText = if (nameError) {{ Text(Common.required) }} else null
        )

        ExposedDropdownMenuBox(
            expanded = purchaseUnitExpanded,
            onExpandedChange = { purchaseUnitExpanded = it }
        ) {
            OutlinedTextField(
                value = purchaseUnit,
                onValueChange = {},
                readOnly = true,
                label = { Text(Ingredients.purchaseUnit) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = purchaseUnitExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true)
            )
            ExposedDropdownMenu(
                expanded = purchaseUnitExpanded,
                onDismissRequest = { purchaseUnitExpanded = false }
            ) {
                purchaseUnits.forEach { unit ->
                    DropdownMenuItem(
                        text = { Text(unit) },
                        onClick = { purchaseUnit = unit; purchaseUnitExpanded = false }
                    )
                }
            }
        }

        OutlinedTextField(
            value = purchasePrice,
            onValueChange = { purchasePrice = it; purchasePriceError = false },
            label = { Text(Ingredients.purchasePrice) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            prefix = { Text("$") },
            isError = purchasePriceError,
            supportingText = if (purchasePriceError) {{ Text(Common.required) }} else null
        )

        OutlinedTextField(
            value = contentAmount,
            onValueChange = { contentAmount = it; contentAmountError = false },
            label = { Text(Ingredients.quantity) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            isError = contentAmountError,
            supportingText = if (contentAmountError) {{ Text(Common.required) }} else null
        )

        ExposedDropdownMenuBox(
            expanded = usageUnitExpanded,
            onExpandedChange = { usageUnitExpanded = it }
        ) {
            OutlinedTextField(
                value = usageUnit,
                onValueChange = {},
                readOnly = true,
                label = { Text(Ingredients.usageUnit) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = usageUnitExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true)
            )
            ExposedDropdownMenu(
                expanded = usageUnitExpanded,
                onDismissRequest = { usageUnitExpanded = false }
            ) {
                usageUnits.forEach { unit ->
                    DropdownMenuItem(
                        text = { Text(unit) },
                        onClick = { usageUnit = unit; usageUnitExpanded = false }
                    )
                }
            }
        }

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
                    nameError = name.isBlank()
                    purchasePriceError = purchasePrice.toDoubleOrNull() == null
                    contentAmountError = contentAmount.toDoubleOrNull() == null

                    if (!nameError && !purchasePriceError && !contentAmountError) {
                        val newIngredient = Ingredient(
                            id = ingredient?.id ?: 0,
                            name = name.trim(),
                            purchaseUnit = purchaseUnit,
                            purchasePrice = purchasePrice.toDouble(),
                            contentAmount = contentAmount.toDouble(),
                            usageUnit = usageUnit,
                            isActive = ingredient?.isActive ?: true,
                            updatedAt = System.currentTimeMillis()
                        )
                        onSave(newIngredient)
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