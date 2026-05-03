package com.recipecostcalculator.di

import android.content.Context
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import app.cash.sqldelight.db.SqlDriver
import com.recipecostcalculator.db.PizzeriaDatabase
import com.recipecostcalculator.data.local.DatabaseSeeder
import com.recipecostcalculator.data.local.repository.AdditionalVariableCostRepositoryImpl
import com.recipecostcalculator.data.local.repository.FixedCostRepositoryImpl
import com.recipecostcalculator.data.local.repository.IngredientRepositoryImpl
import com.recipecostcalculator.data.local.repository.RecipeRepositoryImpl
import com.recipecostcalculator.data.local.repository.SettingsRepositoryImpl
import com.recipecostcalculator.domain.repository.AdditionalVariableCostRepository
import com.recipecostcalculator.domain.repository.FixedCostRepository
import com.recipecostcalculator.domain.repository.IngredientRepository
import com.recipecostcalculator.domain.repository.RecipeRepository
import com.recipecostcalculator.domain.repository.SettingsRepository
import com.recipecostcalculator.domain.usecase.CalculateRecipeCostUseCase
import com.recipecostcalculator.ui.viewmodel.AdditionalVariableCostsViewModel
import com.recipecostcalculator.ui.viewmodel.DashboardViewModel
import com.recipecostcalculator.ui.viewmodel.FixedCostsViewModel
import com.recipecostcalculator.ui.viewmodel.IngredientsViewModel
import com.recipecostcalculator.ui.viewmodel.RecipesViewModel
import com.recipecostcalculator.ui.viewmodel.SettingsViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val androidAppModule = module {
    single<SqlDriver> {
        AndroidSqliteDriver(
            schema = PizzeriaDatabase.Schema,
            context = androidContext(),
            name = "pizzeria.db"
        )
    }

    single<PizzeriaDatabase> { PizzeriaDatabase(get()) }

    single<IngredientRepository> { IngredientRepositoryImpl(get()) }
    single<RecipeRepository> { RecipeRepositoryImpl(get()) }
    single<AdditionalVariableCostRepository> { AdditionalVariableCostRepositoryImpl(get()) }
    single<FixedCostRepository> { FixedCostRepositoryImpl(get()) }
    single<SettingsRepository> { SettingsRepositoryImpl(get()) }

    factory<CalculateRecipeCostUseCase> {
        CalculateRecipeCostUseCase(
            recipeRepository = get(),
            ingredientRepository = get(),
            additionalCostRepository = get(),
            fixedCostRepository = get(),
            settingsRepository = get()
        )
    }

    single<DatabaseSeeder> { DatabaseSeeder(get(), get(), get(), get()) }

    viewModel<IngredientsViewModel> { IngredientsViewModel(ingredientRepository = get()) }
    viewModel<RecipesViewModel> { RecipesViewModel(recipeRepository = get(), calculateRecipeCostUseCase = get()) }
    viewModel<FixedCostsViewModel> { FixedCostsViewModel(fixedCostRepository = get()) }
    viewModel<AdditionalVariableCostsViewModel> { AdditionalVariableCostsViewModel(additionalCostRepository = get()) }
    viewModel<DashboardViewModel> {
        DashboardViewModel(
            recipeRepository = get(),
            fixedCostRepository = get(),
            settingsRepository = get(),
            calculateRecipeCostUseCase = get()
        )
    }
    viewModel<SettingsViewModel> { SettingsViewModel(get()) }
}