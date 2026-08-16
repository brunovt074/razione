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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import com.recipecostcalculator.financial.domain.model.Money
import com.recipecostcalculator.financial.domain.model.Quantity
import com.recipecostcalculator.measurement.MeasurementDimension
import com.recipecostcalculator.measurement.MeasurementUnit
import com.recipecostcalculator.measurement.UnitConverter
import com.recipecostcalculator.ui.viewmodel.IngredientsViewModel
import com.recipecostcalculator.ui.strings.es.Common
import com.recipecostcalculator.ui.strings.es.Ingredients

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
                viewModel.onUpdatePrice(ingredient.id, newPrice.amount)
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
    val unitCost = ingredient.unitCost()
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
            val contentInPurchaseUnit = ingredient.contentAmount.convertTo(ingredient.purchaseUnit)
            Text(
                text = Ingredients.purchaseSummary.format(
                    String.format("%.2f", ingredient.purchasePrice.amount),
                    String.format("%.3f", contentInPurchaseUnit.value).trimEnd('0').trimEnd('.'),
                    ingredient.purchaseUnit.label
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            ingredient.details?.let { label ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "$${String.format("%.2f", unitCost.amount)} / ${ingredient.usageUnit.label}",
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

@Suppress("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IngredientForm(
    ingredient: Ingredient?,
    onSave: (Ingredient) -> Unit,
    onCancel: () -> Unit
) {
    var name by remember { mutableStateOf(ingredient?.name ?: "") }
    var dimension by remember { mutableStateOf(ingredient?.dimension ?: MeasurementDimension.MASS) }
    var purchaseUnit by remember { mutableStateOf(ingredient?.purchaseUnit ?: MeasurementUnit.KG) }
    var purchasePackageLabel by remember { mutableStateOf(ingredient?.details ?: "") }
    var purchasePrice by remember { mutableStateOf(ingredient?.purchasePrice?.amount?.toString() ?: "") }
    var contentAmountStr by remember {
        mutableStateOf(
            ingredient?.let {
                val inPurchaseUnit = it.contentAmount.convertTo(it.purchaseUnit)
                String.format("%.3f", inPurchaseUnit.value).trimEnd('0').trimEnd('.')
            } ?: ""
        )
    }
    var usageUnit by remember { mutableStateOf(ingredient?.usageUnit ?: purchaseUnit) }

    var nameError by remember { mutableStateOf(false) }
    var purchasePriceError by remember { mutableStateOf(false) }
    var contentAmountError by remember { mutableStateOf(false) }
    var dimensionExpanded by remember { mutableStateOf(false) }
    var purchaseUnitExpanded by remember { mutableStateOf(false) }
    var usageUnitExpanded by remember { mutableStateOf(false) }

    val availableUnits = MeasurementUnit.unitsFor(dimension)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
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
            expanded = dimensionExpanded,
            onExpandedChange = { dimensionExpanded = it }
        ) {
            OutlinedTextField(
                value = dimensionLabel(dimension),
                onValueChange = {},
                readOnly = true,
                label = { Text(Ingredients.dimension) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dimensionExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true)
            )
            ExposedDropdownMenu(
                expanded = dimensionExpanded,
                onDismissRequest = { dimensionExpanded = false }
            ) {
                MeasurementDimension.values().forEach { dim ->
                    DropdownMenuItem(
                        text = { Text(dimensionLabel(dim)) },
                        onClick = {
                            dimension = dim
                            purchaseUnit = MeasurementUnit.canonicalFor(dim)
                            usageUnit = MeasurementUnit.canonicalFor(dim)
                            dimensionExpanded = false
                        }
                    )
                }
            }
        }

        ExposedDropdownMenuBox(
            expanded = purchaseUnitExpanded,
            onExpandedChange = { purchaseUnitExpanded = it }
        ) {
            OutlinedTextField(
                value = purchaseUnit.label,
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
                availableUnits.forEach { unit ->
                    DropdownMenuItem(
                        text = { Text(unit.label) },
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
            supportingText = {
                if (purchasePriceError) Text(Common.required) else Text(Ingredients.purchasePriceHelper)
            }
        )

        OutlinedTextField(
            value = contentAmountStr,
            onValueChange = { contentAmountStr = it; contentAmountError = false },
            label = { Text("${Ingredients.contentAmount} (${purchaseUnit.label})") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            isError = contentAmountError,
            supportingText = {
                if (contentAmountError) Text(Common.required) else Text(Ingredients.contentAmountHelper)
            }
        )

        ExposedDropdownMenuBox(
            expanded = usageUnitExpanded,
            onExpandedChange = { usageUnitExpanded = it }
        ) {
            OutlinedTextField(
                value = usageUnit.label,
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
                availableUnits.forEach { unit ->
                    DropdownMenuItem(
                        text = { Text(unit.label) },
                        onClick = { usageUnit = unit; usageUnitExpanded = false }
                    )
                }
            }
        }

        val previewPrice = purchasePrice.toDoubleOrNull()
        val previewContent = contentAmountStr.toDoubleOrNull()
        if (previewPrice != null && previewPrice > 0 && previewContent != null && previewContent > 0 &&
            purchaseUnit.dimension == dimension && usageUnit.dimension == dimension
        ) {
            val previewIngredient = Ingredient(
                name = name.ifBlank { "-" },
                dimension = dimension,
                purchaseUnit = purchaseUnit,
                purchasePrice = Money(previewPrice),
                contentAmount = Quantity(UnitConverter.toCanonical(previewContent, purchaseUnit), MeasurementUnit.canonicalFor(dimension)),
                usageUnit = usageUnit
            )
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = Ingredients.purchaseSummary.format(
                            String.format("%.2f", previewPrice),
                            String.format("%.3f", previewContent).trimEnd('0').trimEnd('.'),
                            purchaseUnit.label
                        ),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "${Ingredients.unitCost}: $${String.format("%.2f", previewIngredient.unitCost().amount)} / ${usageUnit.label}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        OutlinedTextField(
            value = purchasePackageLabel,
            onValueChange = { purchasePackageLabel = it },
            label = { Text(Ingredients.purchasePackageLabel) },
            modifier = Modifier.fillMaxWidth()
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
                    nameError = name.isBlank()
                    purchasePriceError = purchasePrice.toDoubleOrNull() == null
                    contentAmountError = contentAmountStr.toDoubleOrNull() == null

                    if (!nameError && !purchasePriceError && !contentAmountError) {
                        val contentInPurchaseUnit = contentAmountStr.toDouble()
                        val canonical = MeasurementUnit.canonicalFor(dimension)
                        val canonicalValue = UnitConverter.toCanonical(contentInPurchaseUnit, purchaseUnit)
                        val newIngredient = Ingredient(
                            id = ingredient?.id ?: 0,
                            name = name.trim(),
                            dimension = dimension,
                            purchaseUnit = purchaseUnit,
                            details = purchasePackageLabel.trim().ifBlank { null },
                            purchasePrice = Money(purchasePrice.toDouble()),
                            contentAmount = Quantity(canonicalValue, canonical),
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

private fun dimensionLabel(dimension: MeasurementDimension): String = when (dimension) {
    MeasurementDimension.MASS -> Ingredients.dimensionMass
    MeasurementDimension.VOLUME -> Ingredients.dimensionVolume
    MeasurementDimension.COUNT -> Ingredients.dimensionCount
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PriceUpdateDialog(
    currentPrice: Money,
    onDismiss: () -> Unit,
    onConfirm: (Money) -> Unit
) {
    var priceStr by remember { mutableStateOf(currentPrice.amount.toString()) }
    var priceError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(Ingredients.updatePrice) },
        text = {
            OutlinedTextField(
                value = priceStr,
                onValueChange = { priceStr = it; priceError = false },
                label = { Text(Ingredients.price) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                prefix = { Text("$") },
                isError = priceError
            )
        },
        confirmButton = {
            TextButton(onClick = {
                val amount = priceStr.toDoubleOrNull()
                if (amount != null && amount > 0) {
                    onConfirm(Money(amount))
                } else {
                    priceError = true
                }
            }) {
                Text(Common.save)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(Common.cancel)
            }
        }
    )
}
