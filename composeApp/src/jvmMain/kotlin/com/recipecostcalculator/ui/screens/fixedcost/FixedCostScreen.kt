package com.recipecostcalculator.ui.screens.fixedcost

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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

@Composable
fun FixedCostScreen(
    presenter: FixedCostPresenter,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Gastos fijos", style = MaterialTheme.typography.headlineSmall)

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text("Cargar gasto fijo mensual", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = presenter.conceptNameInput,
                    onValueChange = presenter::updateConceptNameInput,
                    label = { Text("Concepto") },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = presenter.conceptAmountInput,
                    onValueChange = presenter::updateConceptAmountInput,
                    label = { Text("Monto mensual") },
                    placeholder = { Text("Ej: 120000") },
                    modifier = Modifier.fillMaxWidth(),
                )
                Button(onClick = presenter::createFixedCostItem) {
                    Text("Guardar gasto fijo")
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text("Produccion mensual", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = presenter.productionInput,
                    onValueChange = presenter::updateProductionInput,
                    label = { Text("Pizzas por mes") },
                    placeholder = { Text("Ej: 600") },
                    modifier = Modifier.fillMaxWidth(),
                )
                Button(onClick = presenter::saveProductionPerMonth) {
                    Text("Guardar produccion")
                }
            }
        }

        presenter.summary?.let { summary ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text("Resumen", style = MaterialTheme.typography.titleMedium)
                    Text("Total gastos fijos mensuales: ${summary.totalMonthlyFixedCost.toDisplay()}")
                    Text("Produccion mensual: ${summary.productionPerMonth.stripTrailingZeros().toPlainString()}")
                    Text("Gasto fijo por pizza: ${summary.fixedCostPerPizza.toDisplay()}")
                }
            }
        }

        Text("Listado", style = MaterialTheme.typography.titleMedium)
        if (presenter.items.isEmpty()) {
            Text("No hay gastos fijos cargados")
        } else {
            presenter.items.forEach { item ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(item.name)
                        Text(item.monthlyAmount.toDisplay())
                    }
                }
            }
        }

        presenter.message?.let { Text(it) }
    }
}
