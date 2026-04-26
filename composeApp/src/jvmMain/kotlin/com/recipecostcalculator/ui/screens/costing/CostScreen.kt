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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.recipecostcalculator.ui.components.SimpleDropdown

@Composable
fun CostScreen(
    presenter: CostPresenter,
) {
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
            options = presenter.recipes,
            selected = presenter.currentSelectedRecipe(),
            optionLabel = { it.name },
            onSelected = presenter::selectRecipe,
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = presenter::calculate,
            ) {
                Text("Calcular")
            }

            Button(
                onClick = presenter::refresh,
            ) {
                Text("Recargar")
            }
        }

        presenter.costResult?.let { dto ->
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

        presenter.message?.let { Text(it) }
    }
}
