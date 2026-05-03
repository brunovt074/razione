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
import com.recipecostcalculator.ui.viewmodel.DashboardViewModel
import com.recipecostcalculator.ui.viewmodel.FixedCostsViewModel
import com.recipecostcalculator.ui.viewmodel.IngredientsViewModel
import com.recipecostcalculator.ui.viewmodel.RecipesViewModel
import com.recipecostcalculator.ui.viewmodel.SettingsViewModel
import com.recipecostcalculator.ui.screens.configuration.ConfigurationScreen
import com.recipecostcalculator.ui.screens.dashboard.DashboardScreen
import com.recipecostcalculator.ui.screens.fixedcosts.FixedCostsScreen
import com.recipecostcalculator.ui.screens.ingredients.IngredientsScreen
import com.recipecostcalculator.ui.screens.more.MoreScreen
import com.recipecostcalculator.ui.screens.recipes.RecipeDetailScreen
import com.recipecostcalculator.ui.screens.recipes.RecipesScreen
import com.recipecostcalculator.ui.strings.es.Navigation

@Composable
fun App(
    dashboardViewModel: DashboardViewModel,
    recipesViewModel: RecipesViewModel,
    ingredientsViewModel: IngredientsViewModel,
    fixedCostsViewModel: FixedCostsViewModel,
    settingsViewModel: SettingsViewModel
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var showFixedCosts by remember { mutableStateOf(false) }
    var showConfiguration by remember { mutableStateOf(false) }
    var selectedRecipeId by remember { mutableStateOf<Long?>(null) }

    val tabs = listOf(
        TabItem(Navigation.home, "H") { DashboardScreen(dashboardViewModel) },
        TabItem(Navigation.recipes, "R") { 
            if (selectedRecipeId != null) {
                RecipeDetailScreenWrapper(
                    recipeId = selectedRecipeId,
                    recipesViewModel = recipesViewModel,
                    onBack = { selectedRecipeId = null }
                )
            } else {
                RecipesScreen(
                    viewModel = recipesViewModel,
                    onRecipeClick = { id -> selectedRecipeId = id }
                )
            }
        },
        TabItem(Navigation.ingredients, "I") { IngredientsScreen(ingredientsViewModel) },
        TabItem(Navigation.more, "M") { 
            MoreScreen(
                fixedCostsViewModel = fixedCostsViewModel,
                settingsViewModel = settingsViewModel,
                onNavigateToFixedCosts = { showFixedCosts = true },
                onNavigateToConfiguration = { showConfiguration = true }
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
                showFixedCosts -> FixedCostsScreen(
                    viewModel = fixedCostsViewModel,
                    onBack = { showFixedCosts = false }
                )
                showConfiguration -> ConfigurationScreen(
                    settingsViewModel = settingsViewModel,
                    onBack = { showConfiguration = false }
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
private fun RecipeDetailScreenWrapper(
    recipeId: Long?,
    recipesViewModel: RecipesViewModel,
    onBack: () -> Unit
) {
    RecipeDetailScreen(
        recipeId = recipeId,
        viewModel = recipesViewModel,
        onBack = onBack
    )
}