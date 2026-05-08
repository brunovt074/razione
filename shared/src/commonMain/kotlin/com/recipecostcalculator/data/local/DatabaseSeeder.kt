package com.recipecostcalculator.data.local

import com.recipecostcalculator.domain.model.AdditionalVariableCost
import com.recipecostcalculator.domain.model.AppSettings
import com.recipecostcalculator.domain.model.Ingredient
import com.recipecostcalculator.domain.model.Recipe
import com.recipecostcalculator.domain.model.RecipeIngredient
import com.recipecostcalculator.domain.repository.AdditionalVariableCostRepository
import com.recipecostcalculator.domain.repository.IngredientRepository
import com.recipecostcalculator.domain.repository.RecipeRepository
import com.recipecostcalculator.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.first

class DatabaseSeeder(
    private val ingredientRepository: IngredientRepository,
    private val recipeRepository: RecipeRepository,
    private val additionalCostRepository: AdditionalVariableCostRepository,
    private val settingsRepository: SettingsRepository
) {
    suspend fun seedIfEmpty() {
        val ingredients = ingredientRepository.observeAll().first()
        if (ingredients.isNotEmpty()) return

        seedIngredients()
        seedRecipes()
        seedAdditionalCosts()
        seedSettings()
    }

    private suspend fun seedIngredients() {
        val now = System.currentTimeMillis()
        val ingredients = listOf(
            Ingredient(name = "Harina", purchaseUnit = "kg", purchasePrice = 1200.0, contentAmount = 1.0, usageUnit = "g", updatedAt = now),
            Ingredient(name = "Mozzarella", purchaseUnit = "kg", purchasePrice = 4500.0, contentAmount = 1.0, usageUnit = "g", updatedAt = now),
            Ingredient(name = "Salsa de tomate", purchaseUnit = "lt", purchasePrice = 1800.0, contentAmount = 1.0, usageUnit = "cc", updatedAt = now),
            Ingredient(name = "Levadura", purchaseUnit = "kg", purchasePrice = 3500.0, contentAmount = 1.0, usageUnit = "g", updatedAt = now),
            Ingredient(name = "Aceite", purchaseUnit = "lt", purchasePrice = 2200.0, contentAmount = 1.0, usageUnit = "cc", updatedAt = now),
            Ingredient(name = "Sal", purchaseUnit = "kg", purchasePrice = 800.0, contentAmount = 1.0, usageUnit = "g", updatedAt = now),
            Ingredient(name = "Cebolla", purchaseUnit = "kg", purchasePrice = 600.0, contentAmount = 1.0, usageUnit = "g", updatedAt = now)
        )
        ingredients.forEach { ingredientRepository.insert(it) }
    }

    private suspend fun seedRecipes() {
        val now = System.currentTimeMillis()
        val muzzarella = Recipe(
            name = "Muzzarela",
            parentRecipeId = null,
            createdAt = now,
            updatedAt = now
        )
        val muzzarellaId = recipeRepository.insert(muzzarella)

        val fugazzeta = Recipe(
            name = "Fugazzeta",
            parentRecipeId = muzzarellaId,
            createdAt = now,
            updatedAt = now
        )
        val fugazzetaId = recipeRepository.insert(fugazzeta)

        val ingredientes = ingredientRepository.observeAll().first()

        val muzzarellaIngredients = listOf(
            RecipeIngredient(recipeId = muzzarellaId, ingredientId = ingredientes.first { it.name == "Harina" }.id, usagePerPizza = 250.0, yieldPizzas = null),
            RecipeIngredient(recipeId = muzzarellaId, ingredientId = ingredientes.first { it.name == "Mozzarella" }.id, usagePerPizza = 150.0, yieldPizzas = null),
            RecipeIngredient(recipeId = muzzarellaId, ingredientId = ingredientes.first { it.name == "Salsa de tomate" }.id, usagePerPizza = 80.0, yieldPizzas = null),
            RecipeIngredient(recipeId = muzzarellaId, ingredientId = ingredientes.first { it.name == "Levadura" }.id, usagePerPizza = 10.0, yieldPizzas = null),
            RecipeIngredient(recipeId = muzzarellaId, ingredientId = ingredientes.first { it.name == "Aceite" }.id, usagePerPizza = 15.0, yieldPizzas = null),
            RecipeIngredient(recipeId = muzzarellaId, ingredientId = ingredientes.first { it.name == "Sal" }.id, usagePerPizza = 5.0, yieldPizzas = null)
        )
        recipeRepository.setIngredients(muzzarellaId, muzzarellaIngredients)

        val fugazzetaIngredients = listOf(
            RecipeIngredient(recipeId = fugazzetaId, ingredientId = ingredientes.first { it.name == "Cebolla" }.id, usagePerPizza = 100.0, yieldPizzas = null)
        )
        recipeRepository.setIngredients(fugazzetaId, fugazzetaIngredients)
    }

    private suspend fun seedAdditionalCosts() {
        val cost = AdditionalVariableCost(
            recipeId = null,
            concept = "Bandeja de telgopor",
            unitCost = 50.0,
            note = "Por pizza"
        )
        additionalCostRepository.insert(cost)
    }

    private suspend fun seedSettings() {
        settingsRepository.updateSettings(AppSettings(estimatedMonthlyProduction = 500))
    }
}
