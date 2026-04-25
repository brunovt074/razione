package com.recipecostcalculator.ui.screens.costing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.recipecostcalculator.application.dto.RecipeCostDto
import com.recipecostcalculator.application.usecase.costing.CalculateRecipeCostUseCase
import com.recipecostcalculator.domain.repository.RecipeRepository
import com.recipecostcalculator.ui.components.SimpleDropdown

@Composable
fun CostScreen(
    recipeRepository: RecipeRepository,
    calculateRecipeCostUseCase: CalculateRecipeCostUseCase,
) {
    var refreshKey by remember { mutableStateOf(0) }
    val recipes = remember(refreshKey) { recipeRepository.findAll() }
    var selectedRecipeId by remember { mutableStateOf<Long?>(null) }
    var costResult by remember { mutableStateOf<RecipeCostDto?>(null) }
    var message by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Costos", style = MaterialTheme.typography.headlineSmall)

        SimpleDropdown(
            label = "Receta",
            options = recipes,
            selected = recipes.firstOrNull { it.id == selectedRecipeId },
            optionLabel = { it.name },
            onSelected = {
                selectedRecipeId = it.id
                message = null
            },
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    val recipeId = selectedRecipeId
                    if (recipeId == null) {
                        message = "Selecciona una receta"
                        return@Button
                    }

                    val result = calculateRecipeCostUseCase(
                        CalculateRecipeCostUseCase.Query(recipeId = recipeId),
                    )
                    costResult = RecipeCostDto.from(result)
                },
            ) {
                Text("Calcular")
            }

            Button(
                onClick = {
                    refreshKey++
                    message = "Recetas recargadas"
                },
            ) {
                Text("Recargar")
            }
        }

        costResult?.let { dto ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text("Receta: ${dto.recipeName}", style = MaterialTheme.typography.titleMedium)
                    Text("Costo total: ${dto.totalCost}")
                    Text("Costo por unidad: ${dto.costPerUnit}")
                    Text("Desglose", style = MaterialTheme.typography.titleSmall)
                    if (dto.breakdown.isEmpty()) {
                        Text("Sin componentes")
                    } else {
                        dto.breakdown.forEach { item ->
                            Text("- ${item.ingredientName}: ${item.quantity} -> ${item.cost}")
                        }
                    }
                }
            }
        }

        message?.let { Text(it) }
    }
}
