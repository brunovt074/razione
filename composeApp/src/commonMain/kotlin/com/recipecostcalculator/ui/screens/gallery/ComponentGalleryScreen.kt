package com.recipecostcalculator.ui.screens.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.recipecostcalculator.ui.components.badge.RazioneStatus
import com.recipecostcalculator.ui.components.badge.StatusBadge
import com.recipecostcalculator.ui.components.button.RazioneButton
import com.recipecostcalculator.ui.components.button.RazioneButtonVariant
import com.recipecostcalculator.ui.components.feedback.RazioneNotice
import com.recipecostcalculator.ui.components.icon.RazioneIcons
import com.recipecostcalculator.ui.components.input.QuantityUnitField
import com.recipecostcalculator.ui.components.input.RazioneTextField
import com.recipecostcalculator.ui.components.input.RazioneUnit
import com.recipecostcalculator.ui.components.list.RazioneListRow
import com.recipecostcalculator.ui.components.navigation.RazioneBottomBar
import com.recipecostcalculator.ui.components.navigation.RazioneNavDestination
import com.recipecostcalculator.ui.components.surface.RazioneCard
import com.recipecostcalculator.ui.strings.es.Status
import com.recipecostcalculator.ui.theme.RazioneSpacing
import com.recipecostcalculator.ui.theme.RazioneTheme

private data class IconSample(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

@Composable
fun ComponentGalleryScreen(onBack: () -> Unit) {
    var darkTheme by remember { mutableStateOf(true) }

    RazioneTheme(darkTheme = darkTheme) {
        val colors = RazioneTheme.colors
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background)
                .verticalScroll(rememberScrollState())
                .padding(RazioneSpacing.l),
            verticalArrangement = Arrangement.spacedBy(RazioneSpacing.xxl)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Galería de componentes", style = RazioneTheme.typography.title, color = colors.textPrimary)
                RazioneButton(
                    text = if (darkTheme) "Ver claro" else "Ver oscuro",
                    onClick = { darkTheme = !darkTheme },
                    variant = RazioneButtonVariant.Secondary
                )
            }

            GallerySection(title = "Tipografía") {
                Text("Costo por plato", style = RazioneTheme.typography.display, color = colors.textPrimary)
                Text("Mis recetas", style = RazioneTheme.typography.title, color = colors.textPrimary)
                Text("Ingredientes", style = RazioneTheme.typography.section, color = colors.textPrimary)
                Text("El precio sugerido se recalcula cuando cambia un insumo.", style = RazioneTheme.typography.body, color = colors.textPrimary)
                Text("Última actualización hace 3 días", style = RazioneTheme.typography.caption, color = colors.textSecondary)
                Text("$ 8.420,50", style = RazioneTheme.typography.figure, color = colors.figureHighlight)
            }

            GallerySection(title = "Botones") {
                Row(horizontalArrangement = Arrangement.spacedBy(RazioneSpacing.m)) {
                    RazioneButton("Calcular precio", {}, variant = RazioneButtonVariant.Primary)
                    RazioneButton("Duplicar receta", {}, variant = RazioneButtonVariant.Secondary)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(RazioneSpacing.m)) {
                    RazioneButton("Eliminar", {}, variant = RazioneButtonVariant.Destructive)
                    RazioneButton("Ver detalle", {}, variant = RazioneButtonVariant.Text)
                    RazioneButton("Sin cambios para guardar", {}, enabled = false)
                }
            }

            GallerySection(title = "Campos") {
                var name by remember { mutableStateOf("Harina de trigo") }
                var quantity by remember { mutableStateOf("0") }
                var unit by remember { mutableStateOf(RazioneUnit.Gram) }
                RazioneTextField(value = name, onValueChange = { name = it }, label = "Nombre del insumo")
                RazioneTextField(
                    value = "",
                    onValueChange = {},
                    label = "Cantidad",
                    errorMessage = "La cantidad debe ser mayor a cero."
                )
                QuantityUnitField(
                    quantity = quantity,
                    onDecrease = { quantity = ((quantity.toIntOrNull() ?: 0) - 1).coerceAtLeast(0).toString() },
                    onIncrease = { quantity = ((quantity.toIntOrNull() ?: 0) + 1).toString() },
                    unit = unit,
                    onUnitChange = { unit = it }
                )
            }

            GallerySection(title = "Distintivos y avisos") {
                Row(horizontalArrangement = Arrangement.spacedBy(RazioneSpacing.s)) {
                    StatusBadge(RazioneStatus.Profitable, Status.profitable, percentage = "62%")
                    StatusBadge(RazioneStatus.Tight, Status.tight, percentage = "18%")
                    StatusBadge(RazioneStatus.AtLoss, Status.atLoss, percentage = "−4%")
                    StatusBadge(RazioneStatus.Estimated, Status.estimated)
                }
                RazioneNotice(
                    title = "3 insumos con precio vencido",
                    detail = "El costo mostrado puede estar por debajo del real."
                )
            }

            GallerySection(title = "Filas de lista") {
                RazioneCard {
                    RazioneListRow(name = "Calabaza", figure = "$ 1.040", detail = "800 g")
                    RazioneListRow(name = "Queso fresco", figure = "$ 1.650", detail = "300 g · precio de hace 21 días")
                    RazioneListRow(name = "Huevo", figure = "$ 690", detail = "3 u")
                }
            }

            GallerySection(title = "Navegación") {
                var selected by remember { mutableStateOf(0) }
                RazioneBottomBar(
                    destinations = listOf(
                        RazioneNavDestination("Recetas", RazioneIcons.recipe),
                        RazioneNavDestination("Insumos", RazioneIcons.ingredient),
                        RazioneNavDestination("Margen", RazioneIcons.margin),
                        RazioneNavDestination("Cuenta", RazioneIcons.currency)
                    ),
                    selectedIndex = selected,
                    onSelect = { selected = it }
                )
            }

            GallerySection(title = "Iconografía") {
                val icons = listOf(
                    IconSample("Receta", RazioneIcons.recipe),
                    IconSample("Insumo", RazioneIcons.ingredient),
                    IconSample("Balanza", RazioneIcons.scale),
                    IconSample("Costo", RazioneIcons.cost),
                    IconSample("Margen", RazioneIcons.margin),
                    IconSample("Inventario", RazioneIcons.inventory),
                    IconSample("Proveedor", RazioneIcons.supplier),
                    IconSample("Porciones", RazioneIcons.servings),
                    IconSample("Tiempo", RazioneIcons.time),
                    IconSample("Calculadora", RazioneIcons.calculator),
                    IconSample("Moneda", RazioneIcons.currency),
                    IconSample("Alerta", RazioneIcons.alert),
                    IconSample("Buscar", RazioneIcons.search),
                    IconSample("Filtro", RazioneIcons.filter),
                    IconSample("Bowl", RazioneIcons.bowl),
                    IconSample("Agregar", RazioneIcons.add)
                )
                val rows = icons.chunked(4)
                rows.forEach { rowIcons ->
                    Row(horizontalArrangement = Arrangement.spacedBy(RazioneSpacing.xl)) {
                        rowIcons.forEach { sample ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(sample.icon, contentDescription = sample.label, tint = colors.textPrimary, modifier = Modifier.size(24.dp))
                                Text(sample.label, style = RazioneTheme.typography.label, color = colors.textSecondary)
                            }
                        }
                    }
                }
            }

            RazioneButton(text = "Volver", onClick = onBack, variant = RazioneButtonVariant.Text)
        }
    }
}

@Composable
private fun GallerySection(title: String, content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) {
    val colors = RazioneTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(RazioneSpacing.m)) {
        Text(text = title, style = RazioneTheme.typography.section, color = colors.accent)
        content()
    }
}
