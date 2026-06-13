package com.recipecostcalculator

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.recipecostcalculator.ui.strings.es.Navigation
import com.recipecostcalculator.ui.viewmodel.AdditionalVariableCostsViewModel
import com.recipecostcalculator.ui.viewmodel.CategoriesViewModel
import com.recipecostcalculator.ui.viewmodel.DashboardViewModel
import com.recipecostcalculator.ui.viewmodel.FixedCostsViewModel
import com.recipecostcalculator.ui.viewmodel.IngredientsViewModel
import com.recipecostcalculator.ui.viewmodel.RecipesViewModel
import com.recipecostcalculator.ui.viewmodel.SettingsViewModel
import com.recipecostcalculator.ui.screens.additionalvariablecosts.AdditionalVariableCostsScreen
import com.recipecostcalculator.ui.screens.categories.CategoriesScreen
import com.recipecostcalculator.ui.screens.configuration.ConfigurationScreen
import com.recipecostcalculator.ui.screens.dashboard.DashboardScreen
import com.recipecostcalculator.ui.screens.fixedcosts.FixedCostsScreen
import com.recipecostcalculator.ui.screens.ingredients.IngredientsScreen
import com.recipecostcalculator.ui.screens.more.MoreScreen
import com.recipecostcalculator.ui.screens.recipes.RecipeDetailScreen
import com.recipecostcalculator.ui.screens.recipes.RecipesScreen
import kotlinx.coroutines.launch

private sealed class MainRoute {
    data object Tabs : MainRoute()
    data object FixedCosts : MainRoute()
    data object AdditionalCosts : MainRoute()
    data object Configuration : MainRoute()
}

private const val TAB_COUNT = 4

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun App(
    dashboardViewModel: DashboardViewModel,
    recipesViewModel: RecipesViewModel,
    categoriesViewModel: CategoriesViewModel,
    ingredientsViewModel: IngredientsViewModel,
    fixedCostsViewModel: FixedCostsViewModel,
    additionalCostsViewModel: AdditionalVariableCostsViewModel,
    settingsViewModel: SettingsViewModel
) {
    var route by remember { mutableStateOf<MainRoute>(MainRoute.Tabs) }
    var selectedRecipeId by remember { mutableStateOf<Long?>(null) }
    var selectedCategoryId by remember { mutableStateOf<Long?>(null) }
    var selectedCategoryName by remember { mutableStateOf("") }
    val pagerState = rememberPagerState(pageCount = { TAB_COUNT })
    val coroutineScope = rememberCoroutineScope()

    val tabs = listOf(
        TabItem(Navigation.home, Icons.Filled.Home) { DashboardScreen(dashboardViewModel) },
        TabItem(Navigation.recipes, Icons.Filled.Fastfood) {
            when {
                selectedRecipeId != null -> RecipeDetailScreen(
                    recipeId = selectedRecipeId,
                    preselectedCategoryId = selectedCategoryId,
                    viewModel = recipesViewModel,
                    onBack = { selectedRecipeId = null }
                )
                selectedCategoryId != null -> RecipesScreen(
                    viewModel = recipesViewModel,
                    categoryName = selectedCategoryName,
                    onRecipeClick = { id -> selectedRecipeId = id },
                    onBack = {
                        selectedCategoryId = null
                        selectedCategoryName = ""
                        recipesViewModel.setCategory(null)
                    }
                )
                else -> CategoriesScreen(
                    viewModel = categoriesViewModel,
                    onCategoryClick = { id ->
                        val cat = categoriesViewModel.state.value.categories.find { it.id == id }
                        if (cat != null) {
                            selectedCategoryId = id
                            selectedCategoryName = cat.name
                            recipesViewModel.setCategory(id)
                        }
                    }
                )
            }
        },
        TabItem(Navigation.ingredients, Icons.Filled.Inventory2) { IngredientsScreen(ingredientsViewModel) },
        TabItem(Navigation.more, Icons.Filled.MoreVert) {
            MoreScreen(
                onNavigateToFixedCosts = { route = MainRoute.FixedCosts },
                onNavigateToConfiguration = { route = MainRoute.Configuration },
                onNavigateToAdditionalCosts = { route = MainRoute.AdditionalCosts }
            )
        }
    )

    when (route) {
        MainRoute.Tabs -> {
            Scaffold(
                bottomBar = {
                    NavigationBar {
                        tabs.forEachIndexed { index, tab ->
                            NavigationBarItem(
                                icon = { Icon(tab.icon, contentDescription = tab.title) },
                                label = { Text(tab.title) },
                                selected = pagerState.currentPage == index,
                                onClick = {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(index)
                                    }
                                }
                            )
                        }
                    }
                }
            ) { innerPadding ->
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.padding(innerPadding),
                    beyondViewportPageCount = 1
                ) { page ->
                    tabs[page].content()
                }
            }
        }
        MainRoute.FixedCosts -> {
            FixedCostsScreen(
                viewModel = fixedCostsViewModel,
                onBack = { route = MainRoute.Tabs }
            )
        }
        MainRoute.AdditionalCosts -> {
            AdditionalVariableCostsScreen(
                viewModel = additionalCostsViewModel,
                onBack = { route = MainRoute.Tabs }
            )
        }
        MainRoute.Configuration -> {
            ConfigurationScreen(
                settingsViewModel = settingsViewModel,
                onBack = { route = MainRoute.Tabs }
            )
        }
    }
}

data class TabItem(
    val title: String,
    val icon: ImageVector,
    val content: @Composable () -> Unit
)

