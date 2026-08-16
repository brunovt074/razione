package com.recipecostcalculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import com.recipecostcalculator.di.androidAppModule
import com.recipecostcalculator.ui.theme.RazioneTheme
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        startKoin {
            androidLogger()
            androidContext(this@MainActivity)
            modules(androidAppModule)
        }

        val dashboardViewModel: com.recipecostcalculator.ui.viewmodel.DashboardViewModel by inject()
        val recipesViewModel: com.recipecostcalculator.ui.viewmodel.RecipesViewModel by inject()
        val categoriesViewModel: com.recipecostcalculator.ui.viewmodel.CategoriesViewModel by inject()
        val ingredientsViewModel: com.recipecostcalculator.ui.viewmodel.IngredientsViewModel by inject()
        val fixedCostsViewModel: com.recipecostcalculator.ui.viewmodel.FixedCostsViewModel by inject()
        val additionalCostsViewModel: com.recipecostcalculator.ui.viewmodel.AdditionalVariableCostsViewModel by inject()
        val settingsViewModel: com.recipecostcalculator.ui.viewmodel.SettingsViewModel by inject()
        val seeder: com.recipecostcalculator.data.local.DatabaseSeeder by inject()

        setContent {
            RazioneTheme {
                LaunchedEffect(Unit) {
                    seeder.seedIfNeeded()
                }
                App(
                    dashboardViewModel = dashboardViewModel,
                    recipesViewModel = recipesViewModel,
                    categoriesViewModel = categoriesViewModel,
                    ingredientsViewModel = ingredientsViewModel,
                    fixedCostsViewModel = fixedCostsViewModel,
                    additionalCostsViewModel = additionalCostsViewModel,
                    settingsViewModel = settingsViewModel
                )
            }
        }
    }
}