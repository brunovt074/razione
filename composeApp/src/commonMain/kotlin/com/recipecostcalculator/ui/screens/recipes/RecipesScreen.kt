package com.recipecostcalculator.ui.screens.recipes

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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

@Composable
fun RecipesScreen(
    viewModel: RecipesViewModel,
    onRecipeClick: (Long) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var deleteDialogRecipe by remember { mutableStateOf<Recipe?>(null) }
    var includeFixedCosts by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadRecipes()
    }

    Scaffold(
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = Recipes.title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

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
    val baseIngredientCount = cost?.ingredientBreakdown?.count { it.isFromParentRecipe } ?: 0
    val subtitle = if (recipe.parentRecipeId != null) {
        "$baseIngredientCount ${Recipes.ingredientsFromBase}"
    } else {
        Recipes.baseRecipe
    }

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
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${recipe.recipeIngredients.size} ${Recipes.ingredientCount}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            cost?.let { breakdown ->
                val displayCost = if (includeFixedCosts) breakdown.totalCostPerUnit else breakdown.totalVariableCost
                Text(
                    text = "${Recipes.cost} $${String.format("%.2f", displayCost.amount)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                if (includeFixedCosts && !breakdown.fixedCostPerUnit.isZero()) {
                    Text(
                        text = "${Recipes.fixedCostDetail} $${String.format("%.2f", breakdown.fixedCostPerUnit.amount)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}