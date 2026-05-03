package com.recipecostcalculator.ui.screens.dashboard

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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.recipecostcalculator.AppScreen
import com.recipecostcalculator.ui.components.SimpleDropdown

@Composable
fun DashboardScreen(
    presenter: DashboardPresenter,
    onNavigate: (AppScreen) -> Unit,
) {
    val kpi = presenter.buildKpi()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Dashboard", style = MaterialTheme.typography.headlineSmall)
        Text("Resumen rapido de costos y accesos principales")

        if (presenter.recipes.isEmpty()) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text("Primeros pasos", style = MaterialTheme.typography.titleMedium)
                    Text("1) Carga ingredientes")
                    Text("2) Crea tu primera receta")
                    Text("3) Revisa costos por pizza")
                    Text("4) Agrega gastos fijos y precios")
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text("Contexto de calculo", style = MaterialTheme.typography.titleMedium)
                SimpleDropdown(
                    label = "Receta",
                    options = presenter.recipes,
                    selected = presenter.currentSelectedRecipe(),
                    optionLabel = { it.name },
                    onSelected = presenter::selectRecipe,
                )
                OutlinedTextField(
                    value = presenter.loteQuantityInput,
                    onValueChange = presenter::updateLoteQuantityInput,
                    label = { Text("Cantidad de pizzas por lote") },
                    modifier = Modifier.fillMaxWidth(),
                )
                Button(onClick = presenter::calculateSelectedRecipeCost) {
                    Text("Recalcular KPIs")
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { onNavigate(toAppScreen(DashboardAction.GO_TO_COSTOS_POR_PIZZA)) }) {
                Text("Costos por pizza")
            }
            Button(onClick = { onNavigate(toAppScreen(DashboardAction.GO_TO_NUEVA_RECETA)) }) {
                Text("Nueva receta")
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { onNavigate(toAppScreen(DashboardAction.GO_TO_GASTOS_FIJOS)) }) {
                Text("Gastos fijos")
            }
            Button(onClick = { onNavigate(toAppScreen(DashboardAction.GO_TO_LISTA_PRECIOS)) }) {
                Text("Lista de precios")
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { onNavigate(AppScreen.INGREDIENTS) }) {
                Text("Actualizar precios")
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text("KPIs", style = MaterialTheme.typography.titleMedium)
                Text("Costo variable por pizza: ${kpi.variableCostPerPizza}")
                Text("Costo variable por lote: ${kpi.variableCostPerLote}")
                Text("Costo fijo mensual: ${kpi.fixedCostMonthly}")
                Text("Costo fijo por pizza: ${kpi.fixedCostPerPizza}")
                Text("Costo total por pizza: ${kpi.totalCostPerPizza}")
                Text("Precio de venta por pizza: ${kpi.sellingPricePerPizza}")
                Text("Comision/descuento (%): ${kpi.commissionPercent}")
                Text("Ganancia bruta: ${kpi.grossProfit}")
                Text("Margen bruto (%): ${kpi.grossMarginPercent}")
                Text("Precio sugerido incluyendo comision: ${kpi.suggestedPriceTarget}")
            }
        }

        presenter.latestCost?.let { dto ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text("Receta activa: ${dto.recipeName}", style = MaterialTheme.typography.titleSmall)
                    Text("Costo total receta: ${dto.totalCost}")
                    Text("Costo por unidad: ${dto.costPerUnit}")
                }
            }
        }

        presenter.message?.let { Text(it) }
    }
}
