package com.recipecostcalculator.di

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
import org.koin.dsl.module

val appModule = module {
    single<IngredientRepository> { IngredientRepositoryImpl(get()) }
    single<RecipeRepository> { RecipeRepositoryImpl(get()) }
    single<AdditionalVariableCostRepository> { AdditionalVariableCostRepositoryImpl(get()) }
    single<FixedCostRepository> { FixedCostRepositoryImpl(get()) }
    single<SettingsRepository> { SettingsRepositoryImpl(get()) }

    factory { CalculateRecipeCostUseCase(get(), get(), get(), get(), get()) }

    single { DatabaseSeeder(get(), get(), get(), get()) }
}