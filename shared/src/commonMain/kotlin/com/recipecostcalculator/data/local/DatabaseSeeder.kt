package com.recipecostcalculator.data.local

import com.recipecostcalculator.domain.model.AdditionalVariableCost
import com.recipecostcalculator.domain.model.AppSettings
import com.recipecostcalculator.domain.model.Ingredient
import com.recipecostcalculator.domain.model.IngredientUsageMode
import com.recipecostcalculator.domain.model.Recipe
import com.recipecostcalculator.domain.model.RecipeIngredient
import com.recipecostcalculator.domain.repository.AdditionalVariableCostRepository
import com.recipecostcalculator.domain.repository.IngredientRepository
import com.recipecostcalculator.domain.repository.RecipeRepository
import com.recipecostcalculator.domain.repository.SettingsRepository
import com.recipecostcalculator.financial.domain.model.Money
import com.recipecostcalculator.financial.domain.model.Quantity
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
            Ingredient(name = "Harina", purchaseUnit = "kg", purchasePrice = Money.of(18500.0), contentAmount = Quantity.of(25.0), usageUnit = "kg", updatedAt = now),
            Ingredient(name = "Mozzarella", purchaseUnit = "kg", purchasePrice = Money.of(9300.0), contentAmount = Quantity.of(1.0), usageUnit = "kg", updatedAt = now),
            Ingredient(name = "Salsa de tomate", purchaseUnit = "Lata", purchasePrice = Money.of(930.0), contentAmount = Quantity.of(1.0), usageUnit = "Lata", updatedAt = now),
            Ingredient(name = "Levadura", purchaseUnit = "g", purchasePrice = Money.of(3800.0), contentAmount = Quantity.of(500.0), usageUnit = "g", updatedAt = now),
            Ingredient(name = "Aceite", purchaseUnit = "cc", purchasePrice = Money.of(3000.0), contentAmount = Quantity.of(900.0), usageUnit = "cc", updatedAt = now),
            Ingredient(name = "Sal", purchaseUnit = "g", purchasePrice = Money.of(930.0), contentAmount = Quantity.of(500.0), usageUnit = "g", updatedAt = now)
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

        val ingredientes = ingredientRepository.getAllActive()
        val masa = ingredientes.filter { it.name == "Harina" || it.name == "Levadura" || it.name == "Sal" || it.name == "Aceite" }
        val cubierta = ingredientes.filter { it.name == "Mozzarella" || it.name == "Salsa de tomate" }

        val recipeIngredients = mutableListOf<RecipeIngredient>()

        masa.forEach { ing ->
            val mode = when (ing.name) {
                "Harina" -> IngredientUsageMode.ByUsage(Quantity.of(0.3))
                "Levadura" -> IngredientUsageMode.ByUsage(Quantity.of(6.666666666666667))
                "Sal" -> IngredientUsageMode.ByUsage(Quantity.of(4.166666666666667))
                "Aceite" -> IngredientUsageMode.ByUsage(Quantity.of(8.333333333333333))
                else -> IngredientUsageMode.ByUsage(Quantity.of(0.0))
            }
            recipeIngredients.add(
                RecipeIngredient(
                    ingredientId = ing.id,
                    primaryMode = mode
                )
            )
        }

        cubierta.forEach { ing ->
            val mode = when (ing.name) {
                "Mozzarella" -> IngredientUsageMode.ByUsage(Quantity.of(0.25))
                "Salsa de tomate" -> IngredientUsageMode.ByYield(6)
                else -> IngredientUsageMode.ByUsage(Quantity.of(0.0))
            }
            recipeIngredients.add(
                RecipeIngredient(
                    ingredientId = ing.id,
                    primaryMode = mode
                )
            )
        }

        recipeRepository.setIngredients(recipeId, recipeIngredients)
    }

    private suspend fun seedAdditionalCosts() {
        val cost = AdditionalVariableCost(
            recipeId = null,
            concept = "Bandeja de telgopor",
            unitCost = Money.of(230.0),
            note = "Por pizza"
        )
        additionalCostRepository.insert(cost)
    }

    private suspend fun seedSettings() {
        settingsRepository.updateSettings(AppSettings())
    }
}