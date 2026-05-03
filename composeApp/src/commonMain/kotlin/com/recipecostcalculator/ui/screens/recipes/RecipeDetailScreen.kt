package com.recipecostcalculator.ui.screens.recipes

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.recipecostcalculator.domain.model.Ingredient
import com.recipecostcalculator.domain.model.Recipe
import com.recipecostcalculator.domain.model.RecipeIngredient
import com.recipecostcalculator.domain.repository.RecipeRepository
import com.recipecostcalculator.ui.viewmodel.RecipesViewModel
import com.recipecostcalculator.ui.strings.es.Recipes
import com.recipecostcalculator.ui.strings.es.Common
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(
    recipeId: Long?,
    viewModel: RecipesViewModel,
    onBack: () -> Unit
) {
    val recipeRepository: RecipeRepository = koinInject()
    val scope = rememberCoroutineScope()

    val state by viewModel.state.collectAsStateWithLifecycle()
    val allRecipes = state.recipes

    var existingRecipe by remember { mutableStateOf<Recipe?>(null) }
    var name by remember { mutableStateOf("") }
    var parentRecipeId by remember { mutableStateOf<Long?>(null) }
    var parentRecipeExpanded by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(recipeId != null && recipeId != 0L) }
    var isValidating by remember { mutableStateOf(false) }
    var cycleError by remember { mutableStateOf(false) }

    var ownIngredients by remember { mutableStateOf<List<RecipeIngredient>>(emptyList()) }
    var inheritedIngredients by remember { mutableStateOf<List<Ingredient>>(emptyList()) }

    var ingredientToDelete by remember { mutableStateOf<RecipeIngredient?>(null) }

    LaunchedEffect(recipeId) {
        if (recipeId != null && recipeId != 0L) {
            val recipe = recipeRepository.getById(recipeId)
            recipe?.let {
                existingRecipe = it
                name = it.name
                parentRecipeId = it.parentRecipeId
                ownIngredients = it.recipeIngredients

                it.parentRecipeId?.let { parentId ->
                    val parentRecipe = recipeRepository.getById(parentId)
                    inheritedIngredients = parentRecipe?.recipeIngredients?.mapNotNull { ri -> ri.ingredient } ?: emptyList()
                }
            }
            isLoading = false
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
                            .menuAnchor()
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
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                text = Recipes.inheritedIngredients,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            inheritedIngredients.forEach { ingredient ->
                                Text(
                                    text = "• ${ingredient.name} (${ingredient.contentAmount} ${ingredient.usageUnit})",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = Recipes.ownIngredientsHint,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )

                        if (ownIngredients.isEmpty()) {
                            Text(
                                text = Recipes.noIngredientsHint,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            ownIngredients.forEach { ri ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${ri.ingredient?.name ?: "ID: ${ri.ingredientId}"} - ${ri.usagePerPizza} ${ri.ingredient?.usageUnit ?: ""}",
                                        style = MaterialTheme.typography.bodyMedium
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