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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.recipecostcalculator.ingredient.domain.model.Ingredient
import com.recipecostcalculator.recipe.domain.model.UnitOfMeasure
import com.recipecostcalculator.ui.components.SimpleDropdown

@Composable
fun IngredientScreen(
    presenter: IngredientPresenter,
) {
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
                    value = presenter.nameInput,
                    onValueChange = presenter::updateNameInput,
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = presenter.costInput,
                    onValueChange = presenter::updateCostInput,
                    label = { Text("Costo por unidad") },
                    modifier = Modifier.fillMaxWidth(),
                )
                SimpleDropdown(
                    label = "Unidad",
                    options = UnitOfMeasure.entries,
                    selected = presenter.selectedUnit,
                    optionLabel = { "${it.name} (${it.symbol})" },
                    onSelected = presenter::updateSelectedUnit,
                )
                Button(
                    onClick = presenter::createIngredient,
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
                    options = presenter.ingredients,
                    selected = presenter.currentSelectedIngredient(),
                    optionLabel = { "${it.name} (${it.costPerUnit.toDisplay()}/${it.unit.symbol})" },
                    onSelected = presenter::selectIngredient,
                )
                OutlinedTextField(
                    value = presenter.updatedCostInput,
                    onValueChange = presenter::updateUpdatedCostInput,
                    label = { Text("Nuevo costo") },
                    modifier = Modifier.fillMaxWidth(),
                )
                Button(
                    onClick = presenter::updateIngredientCost,
                ) {
                    Text("Actualizar")
                }
            }
        }

        Text("Listado", style = MaterialTheme.typography.titleMedium)
        if (presenter.ingredients.isEmpty()) {
            Text("No hay ingredientes cargados")
        } else {
            presenter.ingredients.forEach { ingredient ->
                IngredientRow(ingredient)
            }
        }

        presenter.message?.let {
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
