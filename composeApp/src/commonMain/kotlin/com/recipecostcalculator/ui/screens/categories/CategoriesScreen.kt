package com.recipecostcalculator.ui.screens.categories

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.recipecostcalculator.domain.model.Category
import com.recipecostcalculator.ui.strings.es.Categories
import com.recipecostcalculator.ui.strings.es.Common
import com.recipecostcalculator.ui.viewmodel.CategoriesViewModel

@Composable
fun CategoriesScreen(
    viewModel: CategoriesViewModel,
    onCategoryClick: (Long) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var deleteCandidateCategory by remember { mutableStateOf<Category?>(null) }
    var editCategory by remember { mutableStateOf<Category?>(null) }
    var showForm by remember { mutableStateOf(false) }

    LaunchedEffect(state.error) {
        if (state.error == "no_delete_has_recipes") {
            deleteCandidateCategory = null
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { editCategory = null; showForm = true }) {
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
                text = Categories.title,
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
            } else if (state.categories.isEmpty()) {
                Text(
                    text = Categories.noCategories,
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.categories) { category ->
                        CategoryCard(
                            category = category,
                            onClick = { onCategoryClick(category.id) },
                            onLongClick = { deleteCandidateCategory = category }
                        )
                    }
                }
            }
        }
    }

    deleteCandidateCategory?.let { category ->
        AlertDialog(
            onDismissRequest = { deleteCandidateCategory = null; viewModel.onErrorShown() },
            title = { Text(Categories.deleteCategory) },
            text = { Text(Categories.deleteCategoryConfirmation.format(category.name)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.onDelete(category.id)
                    deleteCandidateCategory = null
                }) {
                    Text(Common.delete)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteCandidateCategory = null; viewModel.onErrorShown() }) {
                    Text(Common.cancel)
                }
            }
        )
    }

    if (state.error == "no_delete_has_recipes") {
        AlertDialog(
            onDismissRequest = { viewModel.onErrorShown() },
            title = { Text(Categories.deleteCategory) },
            text = { Text(Categories.deleteCategoryBlocked) },
            confirmButton = {
                TextButton(onClick = { viewModel.onErrorShown() }) {
                    Text(Common.accept)
                }
            }
        )
    }

    if (showForm) {
        CategoryFormSheet(
            initial = editCategory,
            onSave = { category ->
                viewModel.onSave(category)
                showForm = false
                editCategory = null
            },
            onDismiss = { showForm = false; editCategory = null }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CategoryCard(
    category: Category,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                val countLabel = if (category.recipeCount == 1) Categories.recipeCountSingular else Categories.recipeCount
                Text(
                    text = "${category.recipeCount} $countLabel",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                category.description?.takeIf { it.isNotBlank() }?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun CategoryFormSheet(
    initial: Category?,
    onSave: (Category) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var description by remember { mutableStateOf(initial?.description ?: "") }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = if (initial == null) Categories.newCategory else Categories.editCategory,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(Categories.categoryName) },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text(Categories.description) },
                supportingText = { Text(Categories.descriptionHelper) },
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                    Text(Common.cancel)
                }
                Button(
                    onClick = {
                        val now = System.currentTimeMillis()
                        onSave(
                            Category(
                                id = initial?.id ?: 0,
                                name = name.trim(),
                                unitLabel = name.trim().lowercase(),
                                description = description.trim().takeIf { it.isNotBlank() },
                                sortOrder = initial?.sortOrder ?: 0,
                                createdAt = initial?.createdAt ?: now,
                                updatedAt = now
                            )
                        )
                    },
                    enabled = name.isNotBlank(),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(Common.save)
                }
            }

            androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(bottom = 16.dp))
        }
    }
}
