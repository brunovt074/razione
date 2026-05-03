package com.recipecostcalculator

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.recipecostcalculator.di.desktopAppModule
import com.recipecostcalculator.ui.viewmodel.AdditionalVariableCostsViewModel
import com.recipecostcalculator.ui.viewmodel.DashboardViewModel
import com.recipecostcalculator.ui.viewmodel.FixedCostsViewModel
import com.recipecostcalculator.ui.viewmodel.IngredientsViewModel
import com.recipecostcalculator.ui.viewmodel.RecipesViewModel
import com.recipecostcalculator.ui.viewmodel.SettingsViewModel
import org.koin.core.context.startKoin
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

fun main() = application {
    startKoin {
        modules(desktopAppModule)
    }

    val injector = object : KoinComponent {
        val dashboardViewModel: DashboardViewModel by inject()
        val recipesViewModel: RecipesViewModel by inject()
        val ingredientsViewModel: IngredientsViewModel by inject()
        val fixedCostsViewModel: FixedCostsViewModel by inject()
        val additionalCostsViewModel: AdditionalVariableCostsViewModel by inject()
        val settingsViewModel: SettingsViewModel by inject()
    }

    Window(
        onCloseRequest = ::exitApplication,
        title = "Recipe Cost Calculator",
    ) {
        App(
            dashboardViewModel = injector.dashboardViewModel,
            recipesViewModel = injector.recipesViewModel,
            ingredientsViewModel = injector.ingredientsViewModel,
            fixedCostsViewModel = injector.fixedCostsViewModel,
            additionalCostsViewModel = injector.additionalCostsViewModel,
            settingsViewModel = injector.settingsViewModel
        )
    }
}