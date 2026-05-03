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
            Ingredient(name = "Harina", purchaseUnit = "kg", purchasePrice = 1200.0, contentAmount = 1.0, usageUnit = "kg", updatedAt = now),
            Ingredient(name = "Mozzarella", purchaseUnit = "kg", purchasePrice = 4500.0, contentAmount = 1.0, usageUnit = "kg", updatedAt = now),
            Ingredient(name = "Salsa de tomate", purchaseUnit = "lt", purchasePrice = 1800.0, contentAmount = 1.0, usageUnit = "lt", updatedAt = now),
            Ingredient(name = "Levadura", purchaseUnit = "kg", purchasePrice = 3500.0, contentAmount = 1.0, usageUnit = "kg", updatedAt = now),
            Ingredient(name = "Aceite", purchaseUnit = "lt", purchasePrice = 2200.0, contentAmount = 1.0, usageUnit = "lt", updatedAt = now),
            Ingredient(name = "Sal", purchaseUnit = "kg", purchasePrice = 800.0, contentAmount = 1.0, usageUnit = "kg", updatedAt = now)
        )
        ingredients.forEach { ingredientRepository.insert(it) }
    }

    private suspend fun seedRecipes() {
        val now = System.currentTimeMillis()
        val recipe = Recipe(
            name = "Muzzarela",
            parentRecipeId = null,
            createdAt = now,
            updatedAt = now
        )
        val recipeId = recipeRepository.insert(recipe)

        val ingredientes = ingredientRepository.observeAll().first()
        val masaIds = ingredientes.filter { it.name == "Harina" || it.name == "Levadura" || it.name == "Sal" || it.name == "Aceite" }
        val cubiertaIds = ingredientes.filter { it.name == "Mozzarella" || it.name == "Salsa de tomate" }

        val recipeIngredients = mutableListOf<RecipeIngredient>()

        masaIds.forEach { ing ->
            recipeIngredients.add(
                RecipeIngredient(
                    recipeId = recipeId,
                    ingredientId = ing.id,
                    usagePerPizza = when (ing.name) {
                        "Harina" -> 0.25
                        "Levadura" -> 0.015
                        "Sal" -> 0.01
                        "Aceite" -> 0.015
                        else -> null
                    },
                    yieldPizzas = null
                )
            )
        }

        cubiertaIds.forEach { ing ->
            recipeIngredients.add(
                RecipeIngredient(
                    recipeId = recipeId,
                    ingredientId = ing.id,
                    usagePerPizza = when (ing.name) {
                        "Mozzarella" -> 0.25
                        "Salsa de tomate" -> 0.15
                        else -> null
                    },
                    yieldPizzas = null
                )
            )
        }

        recipeRepository.setIngredients(recipeId, recipeIngredients)
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
        settingsRepository.updateSettings(AppSettings())
    }
}