package com.recipecostcalculator.ui.screens.recipes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.recipecostcalculator.domain.model.ByUsage
import com.recipecostcalculator.domain.model.ByYield
import com.recipecostcalculator.domain.model.Ingredient
import com.recipecostcalculator.domain.model.IngredientUsageMode
import com.recipecostcalculator.domain.model.Recipe
import com.recipecostcalculator.domain.model.RecipeIngredient
import com.recipecostcalculator.financial.domain.model.Quantity
import com.recipecostcalculator.measurement.MeasurementDimension
import com.recipecostcalculator.measurement.MeasurementUnit
import com.recipecostcalculator.measurement.UnitConverter
import com.recipecostcalculator.domain.model.Category
import com.recipecostcalculator.domain.repository.CategoryRepository
import com.recipecostcalculator.domain.repository.RecipeRepository
import com.recipecostcalculator.ui.viewmodel.RecipesViewModel
import com.recipecostcalculator.ui.viewmodel.IngredientsViewModel
import com.recipecostcalculator.ui.strings.es.Recipes
import com.recipecostcalculator.ui.strings.es.Common
import com.recipecostcalculator.ui.strings.es.Ingredients
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Suppress("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(
    recipeId: Long?,
    preselectedCategoryId: Long?,
    viewModel: RecipesViewModel,
    onBack: () -> Unit
) {
    val recipeRepository: RecipeRepository = koinInject()
    val categoryRepository: CategoryRepository = koinInject()
    val ingredientsViewModel: IngredientsViewModel = koinInject()
    val scope = rememberCoroutineScope()

    val state by viewModel.state.collectAsStateWithLifecycle()
    val ingredientsState by ingredientsViewModel.state.collectAsStateWithLifecycle()
    val allRecipes = state.recipes
    val availableIngredients = ingredientsState.ingredients

    var existingRecipe by remember { mutableStateOf<Recipe?>(null) }
    var name by remember { mutableStateOf("") }
    var parentRecipeId by remember { mutableStateOf<Long?>(null) }
    var selectedCategoryId by remember { mutableStateOf(preselectedCategoryId) }
    var categories by remember { mutableStateOf<List<Category>>(emptyList()) }
    var parentRecipeExpanded by remember { mutableStateOf(false) }
    var categoryExpanded by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(recipeId != null && recipeId != 0L) }
    var isValidating by remember { mutableStateOf(false) }
    var cycleError by remember { mutableStateOf(false) }

    val unitLabel = categories.find { it.id == selectedCategoryId }?.unitLabel ?: "unidades"

    LaunchedEffect(Unit) {
        categories = categoryRepository.getAll()
    }

    var ownIngredients by remember { mutableStateOf<List<RecipeIngredient>>(emptyList()) }
    var inheritedIngredients by remember { mutableStateOf<List<RecipeIngredient>>(emptyList()) }

    var ingredientToDelete by remember { mutableStateOf<RecipeIngredient?>(null) }
    var showIngredientSelector by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        ingredientsViewModel.loadIngredients()
    }

    LaunchedEffect(recipeId) {
        if (recipeId != null && recipeId != 0L) {
            val recipe = recipeRepository.getById(recipeId)
            recipe?.let {
                existingRecipe = it
                name = it.name
                parentRecipeId = it.parentRecipeId
                selectedCategoryId = it.categoryId ?: preselectedCategoryId
            }
            isLoading = false
        }
    }

    LaunchedEffect(existingRecipe, availableIngredients) {
        existingRecipe?.let { recipe ->
            ownIngredients = recipeRepository.getIngredients(recipe.id).map { ri ->
                ri.copy(ingredient = availableIngredients.find { it.id == ri.ingredientId })
            }
            recipe.parentRecipeId?.let { parentId ->
                inheritedIngredients = collectAllBaseIngredients(parentId, availableIngredients, recipeRepository)
            } ?: run {
                inheritedIngredients = emptyList()
            }
        }
    }

    LaunchedEffect(parentRecipeId, availableIngredients) {
        val pid = parentRecipeId
        if (pid != null && availableIngredients.isNotEmpty()) {
            inheritedIngredients = collectAllBaseIngredients(pid, availableIngredients, recipeRepository)
        } else {
            inheritedIngredients = emptyList()
        }
    }

    fun validateAndSave() {
        if (name.isBlank()) return

        if (parentRecipeId != null) {
            scope.launch {
                isValidating = true
                cycleError = hasCycle(
                    Recipe(
                        id = recipeId ?: 0,
                        name = name,
                        parentRecipeId = parentRecipeId,
                        categoryId = selectedCategoryId,
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    ),
                    recipeRepository
                )
                isValidating = false

                if (!cycleError) {
                    val recipe = Recipe(
                        id = existingRecipe?.id ?: 0,
                        name = name.trim(),
                        parentRecipeId = parentRecipeId,
                        categoryId = selectedCategoryId,
                        recipeIngredients = ownIngredients.map { it.copy(recipeId = existingRecipe?.id ?: 0) },
                        createdAt = existingRecipe?.createdAt ?: System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )
                    viewModel.onSave(recipe)
                    onBack()
                }
            }
        } else {
            val recipe = Recipe(
                id = existingRecipe?.id ?: 0,
                name = name.trim(),
                parentRecipeId = null,
                categoryId = selectedCategoryId,
                recipeIngredients = ownIngredients.map { it.copy(recipeId = existingRecipe?.id ?: 0) },
                createdAt = existingRecipe?.createdAt ?: System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            viewModel.onSave(recipe)
            onBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (recipeId == null || recipeId == 0L) Recipes.newRecipe else Recipes.editRecipe) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = Common.back)
                    }
                }
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(Recipes.recipeName) },
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = it }
                ) {
                    OutlinedTextField(
                        value = categories.find { it.id == selectedCategoryId }?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(Recipes.category) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true)
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.name) },
                                onClick = { selectedCategoryId = category.id; categoryExpanded = false }
                            )
                        }
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = parentRecipeExpanded,
                    onExpandedChange = { parentRecipeExpanded = it }
                ) {
                    OutlinedTextField(
                        value = allRecipes.find { it.id == parentRecipeId }?.name ?: Recipes.noParentRecipe,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(Recipes.parentRecipe) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = parentRecipeExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true)
                    )
                    ExposedDropdownMenu(
                        expanded = parentRecipeExpanded,
                        onDismissRequest = { parentRecipeExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(Recipes.noParentRecipe) },
                            onClick = { parentRecipeId = null; parentRecipeExpanded = false }
                        )
                        allRecipes.filter { it.id != (recipeId ?: 0) }.forEach { recipe ->
                            DropdownMenuItem(
                                text = { Text(recipe.name) },
                                onClick = { parentRecipeId = recipe.id; parentRecipeExpanded = false }
                            )
                        }
                    }
                }

                if (isValidating) {
                    Text(
                        text = Common.validatingCycles,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                if (cycleError) {
                    Text(
                        text = Common.cycleError,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                if (parentRecipeId != null && inheritedIngredients.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = Recipes.inheritedIngredients,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            inheritedIngredients.forEach { ri ->
                                val ingredientName = ri.ingredient?.name ?: "ID: ${ri.ingredientId}"
                                val usageDesc = formatUsageForDisplay(ri)
                                val cost = computeUsageCost(ri)
                                val costStr = if (cost != null) " | $${String.format("%.2f", cost)}" else ""
                                Text(
                                    text = "• $ingredientName | $usageDesc$costStr",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showIngredientSelector = true }
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = Recipes.ownIngredients,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )

                        if (ownIngredients.isEmpty()) {
                            Text(
                                text = Recipes.noIngredients,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        } else {
                            ownIngredients.forEach { ri ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val ingredientName = ri.ingredient?.name ?: "ID: ${ri.ingredientId}"
                                    val cost = computeUsageCost(ri)
                                    val costStr = if (cost != null) " | $${String.format("%.2f", cost)}" else ""
                                    Text(
                                        text = "$ingredientName | ${formatUsageForDisplay(ri)}$costStr",
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(onClick = { ingredientToDelete = ri }) {
                                        Text(
                                            text = Common.remove,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onBack,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(Common.cancel)
                    }
                    Button(
                        onClick = { validateAndSave() },
                        modifier = Modifier.weight(1f),
                        enabled = name.isNotBlank() && !isValidating
                    ) {
                        Text(Common.save)
                    }
                }
            }
        }
    }

    ingredientToDelete?.let { ri ->
        AlertDialog(
            onDismissRequest = { ingredientToDelete = null },
            title = { Text(Recipes.deleteIngredient) },
            text = { Text(Recipes.deleteIngredientFromRecipe) },
            confirmButton = {
                TextButton(onClick = {
                    ownIngredients = ownIngredients.filter { it.id != ri.id }
                    ingredientToDelete = null
                }) {
                    Text(Common.delete)
                }
            },
            dismissButton = {
                TextButton(onClick = { ingredientToDelete = null }) {
                    Text(Common.cancel)
                }
            }
        )
    }

    if (showIngredientSelector) {
        IngredientSelectorBottomSheet(
            availableIngredients = availableIngredients,
            unitLabel = unitLabel,
            onIngredientSelected = { ingredient, mode ->
                val recipeIdValue = recipeId ?: 0L
                ownIngredients = ownIngredients + RecipeIngredient(
                    recipeId = recipeIdValue,
                    ingredientId = ingredient.id,
                    primaryMode = mode,
                    ingredient = ingredient
                )
                showIngredientSelector = false
            },
            onDismiss = { showIngredientSelector = false }
        )
    }
}

@Suppress("DefaultLocale")
private fun formatUsageForDisplay(ri: RecipeIngredient, unitLabel: String = "unidades"): String {
    val ingredient = ri.ingredient
    return when (val mode = ri.primaryMode) {
        is ByUsage -> {
            if (ingredient != null) {
                val displayQty = mode.amountPerPizza.convertTo(ingredient.usageUnit)
                val formatted = if (displayQty.value == displayQty.value.toLong().toDouble()) {
                    displayQty.value.toLong().toString()
                } else {
                    String.format("%.3f", displayQty.value).trimEnd('0').trimEnd('.')
                }
                "$formatted ${ingredient.usageUnit.label}"
            } else {
                "${mode.amountPerPizza.value} ${mode.amountPerPizza.unit.label}"
            }
        }
        is ByYield -> Recipes.yieldDisplay(mode.pizzasPerPurchaseUnit, unitLabel)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IngredientSelectorBottomSheet(
    availableIngredients: List<Ingredient>,
    unitLabel: String,
    onIngredientSelected: (Ingredient, IngredientUsageMode) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedIngredient by remember { mutableStateOf<Ingredient?>(null) }
    var usageAmount by remember { mutableStateOf("") }
    var selectedUnit by remember { mutableStateOf<MeasurementUnit?>(null) }
    var isYieldMode by remember { mutableStateOf(true) }
    var ingredientExpanded by remember { mutableStateOf(false) }
    var unitExpanded by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = Recipes.addIngredient,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            ExposedDropdownMenuBox(
                expanded = ingredientExpanded,
                onExpandedChange = { ingredientExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedIngredient?.name ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(Ingredients.name) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = ingredientExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true)
                )
                ExposedDropdownMenu(
                    expanded = ingredientExpanded,
                    onDismissRequest = { ingredientExpanded = false }
                ) {
                    availableIngredients.forEach { ingredient ->
                        DropdownMenuItem(
                            text = { Text(ingredient.name) },
                            onClick = {
                                selectedIngredient = ingredient
                                selectedUnit = ingredient.usageUnit
                                usageAmount = ""
                                isYieldMode = ingredient.dimension == MeasurementDimension.COUNT
                                ingredientExpanded = false
                            }
                        )
                    }
                }
            }

            selectedIngredient?.let { ingredient ->
                val isCount = ingredient.dimension == MeasurementDimension.COUNT

                if (isCount) {
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        SegmentedButton(
                            selected = isYieldMode,
                            onClick = { isYieldMode = true; usageAmount = "" },
                            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                        ) { Text(Recipes.yieldModeOption) }
                        SegmentedButton(
                            selected = !isYieldMode,
                            onClick = { isYieldMode = false; usageAmount = "" },
                            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                        ) { Text(Recipes.usageModeOption) }
                    }

                    if (isYieldMode) {
                        OutlinedTextField(
                            value = usageAmount,
                            onValueChange = { usageAmount = it },
                            label = { Text(Recipes.yieldFieldLabel) },
                            placeholder = { Text(Recipes.yieldPlaceholder(unitLabel)) },
                            supportingText = { Text(Recipes.yieldHelper(unitLabel)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        OutlinedTextField(
                            value = usageAmount,
                            onValueChange = { usageAmount = it },
                            label = { Text(Ingredients.quantity) },
                            suffix = { Text(MeasurementUnit.UN.label) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else {
                    val availableUnits = MeasurementUnit.unitsFor(ingredient.dimension)
                    val currentUnit = selectedUnit ?: ingredient.usageUnit

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = usageAmount,
                            onValueChange = { usageAmount = it },
                            label = { Text(Ingredients.quantity) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )

                        ExposedDropdownMenuBox(
                            expanded = unitExpanded,
                            onExpandedChange = { unitExpanded = it },
                            modifier = Modifier.weight(0.6f)
                        ) {
                            OutlinedTextField(
                                value = currentUnit.label,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text(Ingredients.usageUnit) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true)
                            )
                            ExposedDropdownMenu(
                                expanded = unitExpanded,
                                onDismissRequest = { unitExpanded = false }
                            ) {
                                availableUnits.forEach { unit ->
                                    DropdownMenuItem(
                                        text = { Text(unit.label) },
                                        onClick = { selectedUnit = unit; unitExpanded = false }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(Common.cancel)
                }
                Button(
                    onClick = {
                        val ingredient = selectedIngredient ?: return@Button
                        val isCount = ingredient.dimension == MeasurementDimension.COUNT

                        val mode: IngredientUsageMode? = when {
                            isCount && isYieldMode -> {
                                val yield = usageAmount.toIntOrNull()
                                if (yield != null && yield > 0) ByYield(yield) else null
                            }
                            isCount && !isYieldMode -> {
                                val amount = usageAmount.toDoubleOrNull()
                                if (amount != null && amount > 0)
                                    ByUsage(Quantity(amount, MeasurementUnit.UN))
                                else null
                            }
                            else -> {
                                val amount = usageAmount.toDoubleOrNull()
                                val unit = selectedUnit ?: ingredient.usageUnit
                                if (amount != null && amount > 0) {
                                    val canonicalValue = UnitConverter.toCanonical(amount, unit)
                                    val canonical = MeasurementUnit.canonicalFor(ingredient.dimension)
                                    ByUsage(Quantity(canonicalValue, canonical))
                                } else null
                            }
                        }

                        if (mode != null) onIngredientSelected(ingredient, mode)
                    },
                    modifier = Modifier.weight(1f),
                    enabled = selectedIngredient != null && run {
                        val ingredient = selectedIngredient!!
                        val isCount = ingredient.dimension == MeasurementDimension.COUNT
                        if (isCount && isYieldMode)
                            (usageAmount.toIntOrNull() ?: 0) > 0
                        else
                            (usageAmount.toDoubleOrNull() ?: 0.0) > 0.0
                    }
                ) {
                    Text(Common.add)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

private suspend fun hasCycle(recipe: Recipe, recipeRepository: RecipeRepository): Boolean {
    var currentId = recipe.parentRecipeId
    val visited = mutableSetOf<Long>()
    while (currentId != null) {
        if (currentId == recipe.id || visited.contains(currentId)) return true
        visited.add(currentId)
        currentId = recipeRepository.getById(currentId)?.parentRecipeId
    }
    return false
}

private suspend fun collectAllBaseIngredients(
    parentId: Long,
    availableIngredients: List<Ingredient>,
    recipeRepository: RecipeRepository,
    depth: Int = 0
): List<RecipeIngredient> {
    if (depth >= 5) return emptyList()
    val parent = recipeRepository.getById(parentId) ?: return emptyList()
    val direct = recipeRepository.getIngredients(parent.id).map { ri ->
        ri.copy(ingredient = availableIngredients.find { it.id == ri.ingredientId })
    }
    val fromAncestors = parent.parentRecipeId?.let {
        collectAllBaseIngredients(it, availableIngredients, recipeRepository, depth + 1)
    } ?: emptyList()
    return direct + fromAncestors
}

@Suppress("DefaultLocale")
private fun computeUsageCost(ri: RecipeIngredient): Double? {
    val ingredient = ri.ingredient ?: return null
    return when (val mode = ri.primaryMode) {
        is ByUsage -> {
            val usageInUsageUnit = mode.amountPerPizza.convertTo(ingredient.usageUnit).value
            ingredient.unitCost().amount * usageInUsageUnit
        }
        is ByYield -> ingredient.purchasePrice.amount / mode.pizzasPerPurchaseUnit
    }
}
