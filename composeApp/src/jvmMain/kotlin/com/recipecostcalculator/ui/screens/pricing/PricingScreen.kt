package com.recipecostcalculator.ui.screens.pricing

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
import com.recipecostcalculator.ui.components.SimpleDropdown

@Composable
fun PricingScreen(
    presenter: PricingPresenter,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Costos por pizza", style = MaterialTheme.typography.headlineSmall)

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SimpleDropdown(
                    label = "Receta",
                    options = presenter.recipes,
                    selected = presenter.currentSelectedRecipe(),
                    optionLabel = { it.name },
                    onSelected = presenter::selectRecipe,
                )
                OutlinedTextField(
                    value = presenter.sellingPriceInput,
                    onValueChange = presenter::updateSellingPriceInput,
                    label = { Text("Precio de venta por pizza") },
                    placeholder = { Text("Ej: 6500") },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = presenter.commissionPercentInput,
                    onValueChange = presenter::updateCommissionPercentInput,
                    label = { Text("Comision/descuento (%)") },
                    placeholder = { Text("Ej: 10") },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = presenter.wastePercentInput,
                    onValueChange = presenter::updateWastePercentInput,
                    label = { Text("Merma/desperdicio (%)") },
                    placeholder = { Text("Ej: 3") },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = presenter.targetMarginPercentInput,
                    onValueChange = presenter::updateTargetMarginPercentInput,
                    label = { Text("Margen objetivo (%)") },
                    placeholder = { Text("Ej: 50") },
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = presenter::saveConfiguration) {
                        Text("Guardar configuracion")
                    }
                    Button(onClick = presenter::calculate) {
                        Text("Recalcular")
                    }
                }
            }
        }

        presenter.summary?.let { summary ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text("Resumen comercial", style = MaterialTheme.typography.titleMedium)
                    Text("Costo variable por pizza: ${summary.variableCostPerPizza.toDisplay()}")
                    Text("Costo fijo por pizza: ${summary.fixedCostPerPizza.toDisplay()}")
                    Text("Costo total por pizza: ${summary.totalCostPerPizza.toDisplay()}")
                    Text("Comision/descuento en $: ${summary.commissionAmount.toDisplay()}")
                    Text("Ingreso neto: ${summary.netIncome.toDisplay()}")
                    Text("Ganancia bruta: ${summary.grossProfit.toDisplay()}")
                    Text("Margen bruto (%): ${presenter.toPercentText(summary.grossMarginPercent)}")
                    Text("Markup (%): ${presenter.toPercentText(summary.markupPercent)}")
                    Text("Precio sugerido para margen objetivo: ${summary.suggestedPriceForTargetMargin.toDisplay()}")
                    Text("Precio sugerido incluyendo comision: ${summary.suggestedPriceWithCommission.toDisplay()}")
                }
            }
        }

        presenter.message?.let { Text(it) }
    }
}
