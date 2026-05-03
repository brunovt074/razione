package com.recipecostcalculator

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.recipecostcalculator.presentation.viewmodel.CostosFijosViewModel
import com.recipecostcalculator.presentation.viewmodel.DashboardViewModel
import com.recipecostcalculator.presentation.viewmodel.IngredientesViewModel
import com.recipecostcalculator.presentation.viewmodel.RecetasViewModel
import com.recipecostcalculator.presentation.viewmodel.SettingsViewModel
import com.recipecostcalculator.ui.screens.configuracion.ConfiguracionScreen
import com.recipecostcalculator.ui.screens.costosfijos.CostosFijosScreen
import com.recipecostcalculator.ui.screens.dashboard.DashboardScreen
import com.recipecostcalculator.ui.screens.ingredientes.IngredientesScreen
import com.recipecostcalculator.ui.screens.mas.MasScreen
import com.recipecostcalculator.ui.screens.recetas.RecetaDetailScreen
import com.recipecostcalculator.ui.screens.recetas.RecetasScreen

@Composable
fun App(
    dashboardViewModel: DashboardViewModel,
    recetasViewModel: RecetasViewModel,
    ingredientesViewModel: IngredientesViewModel,
    costosFijosViewModel: CostosFijosViewModel,
    settingsViewModel: SettingsViewModel
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var showCostosFijos by remember { mutableStateOf(false) }
    var showConfiguracion by remember { mutableStateOf(false) }
    var selectedRecipeId by remember { mutableStateOf<Long?>(null) }

    val tabs = listOf(
        TabItem("Inicio", "H") { DashboardScreen(dashboardViewModel) },
        TabItem("Recetas", "R") { 
            if (selectedRecipeId != null) {
                RecetaDetailScreenWrapper(
                    recipeId = selectedRecipeId,
                    recetasViewModel = recetasViewModel,
                    onBack = { selectedRecipeId = null }
                )
            } else {
                RecetasScreen(
                    viewModel = recetasViewModel,
                    onRecipeClick = { selectedRecipeId = it }
                )
            }
        },
        TabItem("Ingredientes", "I") { IngredientesScreen(ingredientesViewModel) },
        TabItem("Más", "M") { 
            MasScreen(
                costosFijosViewModel = costosFijosViewModel,
                settingsViewModel = settingsViewModel,
                onNavigateToCostosFijos = { showCostosFijos = true },
                onNavigateToConfiguracion = { showConfiguracion = true }
            )
        }
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        icon = { Text(tab.icon) },
                        label = { Text(tab.title) },
                        selected = selectedTab == index,
                        onClick = { selectedTab = index }
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding)
        ) {
            when {
                showCostosFijos -> CostosFijosScreen(
                    viewModel = costosFijosViewModel,
                    onBack = { showCostosFijos = false }
                )
                showConfiguracion -> ConfiguracionScreen(
                    settingsViewModel = settingsViewModel,
                    onBack = { showConfiguracion = false }
                )
                else -> tabs[selectedTab].content()
            }
        }
    }
}

data class TabItem(
    val title: String,
    val icon: String,
    val content: @Composable () -> Unit
)

@Composable
private fun RecetaDetailScreenWrapper(
    recipeId: Long?,
    recetasViewModel: RecetasViewModel,
    onBack: () -> Unit
) {
    RecetaDetailScreen(
        recipeId = recipeId,
        viewModel = recetasViewModel,
        onBack = onBack
    )
}