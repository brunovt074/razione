package com.recipecostcalculator.ui.screens.recipes

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
                            onClick = { onRecipeClick(recipe.id) },
                            onLongClick = { deleteDialogRecipe = recipe }
                        )
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
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun RecipeCard(
    recipe: Recipe,
    cost: com.recipecostcalculator.domain.model.CostBreakdown?,
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
            Text(
                text = if (recipe.parentRecipeId != null) Recipes.inheritsFromAnother else Recipes.baseRecipe,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${recipe.recipeIngredients.size} ${Recipes.ingredientCount}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            cost?.let {
                Text(
                    text = "${Recipes.cost} $${String.format("%.2f", it.totalCostPerUnit)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}