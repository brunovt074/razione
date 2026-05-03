package com.recipecostcalculator.ui.screens.pricelist

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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.recipecostcalculator.pricing.domain.model.CostBasisType
import com.recipecostcalculator.ui.components.SimpleDropdown

@Composable
fun PriceListScreen(
    presenter: PriceListPresenter,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Lista de precios", style = MaterialTheme.typography.headlineSmall)

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SimpleDropdown(
                    label = "Costo usado",
                    options = CostBasisType.entries,
                    selected = presenter.costBasisType,
                    optionLabel = { it.name },
                    onSelected = presenter::selectCostBasis,
                )
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text("Agregar producto", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = presenter.productNameInput,
                    onValueChange = presenter::updateProductNameInput,
                    label = { Text("Producto") },
                    placeholder = { Text("Ej: Pizza muzza (congelada)") },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = presenter.sellingPriceInput,
                    onValueChange = presenter::updateSellingPriceInput,
                    label = { Text("Precio de venta") },
                    placeholder = { Text("Ej: 7000") },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = presenter.notesInput,
                    onValueChange = presenter::updateNotesInput,
                    label = { Text("Notas") },
                    modifier = Modifier.fillMaxWidth(),
                )
                Button(onClick = presenter::addProduct) {
                    Text("Guardar producto")
                }
            }
        }

        Text("Productos", style = MaterialTheme.typography.titleMedium)
        if (presenter.rows.isEmpty()) {
            Text("No hay productos cargados")
        } else {
            presenter.rows.forEach { row ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(row.productName)
                        Text("Precio venta: ${row.sellingPrice}")
                        Text("Costo usado: ${row.costUsed}")
                        Text("Ganancia: ${row.grossProfit}")
                        Text("Margen (%): ${row.marginPercent}")
                        if (row.notes.isNotBlank()) {
                            Text("Notas: ${row.notes}")
                        }
                    }
                }
            }
        }

        presenter.message?.let { Text(it) }
    }
}
