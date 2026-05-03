package com.recipecostcalculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material3.Text
import com.recipecostcalculator.di.androidAppModule
import com.recipecostcalculator.di.appModule
import com.recipecostcalculator.ui.theme.AppTheme
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
            modules(appModule, androidAppModule)
        }

        val dashboardViewModel: com.recipecostcalculator.presentation.viewmodel.DashboardViewModel by inject()
        val recetasViewModel: com.recipecostcalculator.presentation.viewmodel.RecetasViewModel by inject()
        val ingredientesViewModel: com.recipecostcalculator.presentation.viewmodel.IngredientesViewModel by inject()
        val costosFijosViewModel: com.recipecostcalculator.presentation.viewmodel.CostosFijosViewModel by inject()
        val settingsViewModel: com.recipecostcalculator.presentation.viewmodel.SettingsViewModel by inject()
        val seeder: com.recipecostcalculator.data.local.DatabaseSeeder by inject()

        setContent {
            AppTheme {
                LaunchedEffect(Unit) {
                    seeder.seedIfEmpty()
                }
                App(
                    dashboardViewModel = dashboardViewModel,
                    recetasViewModel = recetasViewModel,
                    ingredientesViewModel = ingredientesViewModel,
                    costosFijosViewModel = costosFijosViewModel,
                    settingsViewModel = settingsViewModel
                )
            }
        }
    }
}