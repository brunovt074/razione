package com.recipecostcalculator.ui.screens.recipes

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.recipecostcalculator.domain.model.Recipe
import com.recipecostcalculator.ui.viewmodel.RecipesViewModel
import com.recipecostcalculator.ui.strings.es.Recipes
import com.recipecostcalculator.ui.strings.es.Common

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipesScreen(
    viewModel: RecipesViewModel,
    categoryName: String,
    onRecipeClick: (Long) -> Unit,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var deleteDialogRecipe by remember { mutableStateOf<Recipe?>(null) }
    var includeFixedCosts by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadRecipes()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(categoryName) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = Common.back)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onRecipeClick(0) }) {
                Text(Common.addButton)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            if (state.isLoading) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                }
            } else if (state.recipes.isEmpty()) {
                Text(
                    text = Common.noRecipes,
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.recipes) { recipe ->
                        RecipeCard(
                            recipe = recipe,
                            cost = state.costBreakdowns[recipe.id],
                            includeFixedCosts = includeFixedCosts,
                            onClick = { onRecipeClick(recipe.id) },
                            onLongClick = { deleteDialogRecipe = recipe }
                        )
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = includeFixedCosts,
                    onCheckedChange = { includeFixedCosts = it }
                )
                Text(
                    text = Recipes.includeFixedCosts,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }

    deleteDialogRecipe?.let { recipe ->
        AlertDialog(
            onDismissRequest = { deleteDialogRecipe = null },
            title = { Text(Recipes.deleteRecipe) },
            text = { Text(Recipes.deleteRecipeConfirmation.format(recipe.name)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.onDelete(recipe.id)
                    deleteDialogRecipe = null
                }) {
                    Text(Common.delete)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteDialogRecipe = null }) {
                    Text(Common.cancel)
                }
            }
        )
    }
}

@Suppress("DefaultLocale")
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun RecipeCard(
    recipe: Recipe,
    cost: com.recipecostcalculator.domain.model.CostBreakdown?,
    includeFixedCosts: Boolean,
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
                text = recipe.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            cost?.let { breakdown ->
                val baseCount = breakdown.ingredientBreakdown.count { it.isFromParentRecipe }
                val ownCount = breakdown.ingredientBreakdown.count { !it.isFromParentRecipe }
                val displayTotal = if (includeFixedCosts) breakdown.totalCostPerUnit else breakdown.totalVariableCost

                Spacer(modifier = Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    if (baseCount > 0) {
                        BreakdownRow(
                            label = ingredientLabel(baseCount, fromBase = true),
                            amount = breakdown.ingredientCostFromParent.amount
                        )
                    }
                    if (ownCount > 0) {
                        BreakdownRow(
                            label = ingredientLabel(ownCount, fromBase = false),
                            amount = breakdown.ingredientCostOwn.amount
                        )
                    }
                    if (!breakdown.additionalVariableCost.isZero()) {
                        BreakdownRow(
                            label = Recipes.supplies,
                            amount = breakdown.additionalVariableCost.amount
                        )
                    }
                    if (includeFixedCosts && !breakdown.fixedCostPerUnit.isZero()) {
                        BreakdownRow(
                            label = Recipes.fixedCostLine,
                            amount = breakdown.fixedCostPerUnit.amount
                        )
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    BreakdownRow(
                        label = Recipes.totalCost,
                        amount = displayTotal.amount,
                        isTotal = true
                    )
                }
            }
        }
    }
}

@Suppress("DefaultLocale")
@Composable
private fun BreakdownRow(label: String, amount: Double, isTotal: Boolean = false) {
    val style = if (isTotal) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodySmall
    val color = if (isTotal) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    val weight = if (isTotal) FontWeight.Bold else FontWeight.Normal
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = style, color = color, fontWeight = weight)
        Text("$${String.format("%.2f", amount)}", style = style, color = color, fontWeight = weight)
    }
}

private fun ingredientLabel(count: Int, fromBase: Boolean): String {
    val noun = if (count == 1) Recipes.ingredientSingular else Recipes.ingredientCount
    return if (fromBase) "$count $noun ${Recipes.baseRecipeSuffix}" else "$count $noun"
}