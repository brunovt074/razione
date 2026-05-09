package com.recipecostcalculator.di

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
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
import kotlinx.coroutines.Dispatchers
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val desktopAppModule = module {
    single<SqlDriver> {
        JdbcSqliteDriver(
            url = "jdbc:sqlite:pizzeria.db",
            schema = PizzeriaDatabase.Schema
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
    viewModel<RecipesViewModel> {
        RecipesViewModel(
            recipeRepository = get(),
            ingredientRepository = get(),
            additionalCostRepository = get(),
            calculateRecipeCostUseCase = get()
        )
    }
    viewModel<FixedCostsViewModel> { FixedCostsViewModel(fixedCostRepository = get()) }
    viewModel<AdditionalVariableCostsViewModel> { AdditionalVariableCostsViewModel(additionalCostRepository = get()) }
    viewModel<DashboardViewModel> {
        DashboardViewModel(
            recipeRepository = get(),
            ingredientRepository = get(),
            fixedCostRepository = get(),
            additionalCostRepository = get(),
            settingsRepository = get(),
            calculateRecipeCostUseCase = get()
        )
    }
    viewModel<SettingsViewModel> { SettingsViewModel(settingsRepository = get()) }
}