package com.recipecostcalculator.ui.screens.ingredients

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.recipecostcalculator.application.usecase.ingredient.CreateIngredientUseCase
import com.recipecostcalculator.application.usecase.ingredient.UpdateIngredientCostUseCase
import com.recipecostcalculator.domain.model.ingredient.Ingredient
import com.recipecostcalculator.domain.model.valueobject.Money
import com.recipecostcalculator.domain.model.valueobject.UnitOfMeasure
import com.recipecostcalculator.domain.repository.IngredientRepository
import com.recipecostcalculator.ui.components.SimpleDropdown
import com.recipecostcalculator.ui.components.parseDecimalOrNull

@Composable
fun IngredientScreen(
    ingredientRepository: IngredientRepository,
    createIngredientUseCase: CreateIngredientUseCase,
    updateIngredientCostUseCase: UpdateIngredientCostUseCase,
) {
    var refreshKey by remember { mutableStateOf(0) }
    val ingredients = remember(refreshKey) { ingredientRepository.findAll() }

    var nameInput by remember { mutableStateOf("") }
    var costInput by remember { mutableStateOf("") }
    var selectedUnit by remember { mutableStateOf(UnitOfMeasure.GRAM) }

    var selectedIngredientId by remember { mutableStateOf<Long?>(null) }
    var updatedCostInput by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Ingredientes", style = MaterialTheme.typography.headlineSmall)

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text("Crear ingrediente", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = costInput,
                    onValueChange = { costInput = it },
                    label = { Text("Costo por unidad") },
                    modifier = Modifier.fillMaxWidth(),
                )
                SimpleDropdown(
                    label = "Unidad",
                    options = UnitOfMeasure.entries,
                    selected = selectedUnit,
                    optionLabel = { "${it.name} (${it.symbol})" },
                    onSelected = { selectedUnit = it },
                )
                Button(
                    onClick = {
                        val cost = parseDecimalOrNull(costInput)
                        if (nameInput.isBlank() || cost == null) {
                            message = "Completa nombre y costo valido"
                            return@Button
                        }

                        createIngredientUseCase(
                            CreateIngredientUseCase.Command(
                                name = nameInput,
                                unit = selectedUnit,
                                costPerUnit = Money.of(cost),
                            ),
                        )
                        nameInput = ""
                        costInput = ""
                        message = "Ingrediente creado"
                        refreshKey++
                    },
                ) {
                    Text("Guardar")
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text("Actualizar costo", style = MaterialTheme.typography.titleMedium)
                SimpleDropdown(
                    label = "Ingrediente",
                    options = ingredients,
                    selected = ingredients.firstOrNull { it.id == selectedIngredientId },
                    optionLabel = { "${it.name} (${it.costPerUnit.toDisplay()}/${it.unit.symbol})" },
                    onSelected = {
                        selectedIngredientId = it.id
                        updatedCostInput = it.costPerUnit.amount.toPlainString()
                    },
                )
                OutlinedTextField(
                    value = updatedCostInput,
                    onValueChange = { updatedCostInput = it },
                    label = { Text("Nuevo costo") },
                    modifier = Modifier.fillMaxWidth(),
                )
                Button(
                    onClick = {
                        val ingredientId = selectedIngredientId
                        val cost = parseDecimalOrNull(updatedCostInput)
                        if (ingredientId == null || cost == null) {
                            message = "Selecciona ingrediente y costo valido"
                            return@Button
                        }
                        updateIngredientCostUseCase(
                            UpdateIngredientCostUseCase.Command(
                                ingredientId = ingredientId,
                                costPerUnit = Money.of(cost),
                            ),
                        )
                        message = "Costo actualizado"
                        refreshKey++
                    },
                ) {
                    Text("Actualizar")
                }
            }
        }

        Text("Listado", style = MaterialTheme.typography.titleMedium)
        if (ingredients.isEmpty()) {
            Text("No hay ingredientes cargados")
        } else {
            ingredients.forEach { ingredient ->
                IngredientRow(ingredient)
            }
        }

        message?.let {
            Spacer(modifier = Modifier.height(4.dp))
            Text(it)
        }
    }
}

@Composable
private fun IngredientRow(ingredient: Ingredient) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(ingredient.name)
        Text("${ingredient.costPerUnit.toDisplay()} / ${ingredient.unit.symbol}")
    }
}
