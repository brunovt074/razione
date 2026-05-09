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
    companion object {
        private const val CURRENT_SEED_VERSION = 2
    }

    suspend fun seedIfNeeded() {
        val settings = settingsRepository.getSettings()
        if (settings.seedVersion >= CURRENT_SEED_VERSION) return

        clearExistingData()
        seedIngredients()
        seedRecipes()
        seedAdditionalCosts()
        seedSettings()
    }

    private suspend fun clearExistingData() {
        additionalCostRepository.getAll().forEach { additionalCostRepository.delete(it.id) }
        val recipes = recipeRepository.getAll()
        recipes.filter { it.parentRecipeId != null }.forEach { recipeRepository.delete(it.id) }
        recipes.filter { it.parentRecipeId == null }.forEach { recipeRepository.delete(it.id) }
        ingredientRepository.observeAll().first().forEach { ingredientRepository.delete(it.id) }
    }

    private suspend fun seedIngredients() {
        val now = System.currentTimeMillis()
        val ingredients = listOf(
            Ingredient(name = "Harina", purchaseUnit = "Bolsa 25kg", purchasePrice = Money(18500.0), contentAmount = Quantity(25.0), usageUnit = "Kg", updatedAt = now),
            Ingredient(name = "Mozzarella", purchaseUnit = "Kg", purchasePrice = Money(9300.0), contentAmount = Quantity(1.0), usageUnit = "Kg", updatedAt = now),
            Ingredient(name = "Salsa de tomate", purchaseUnit = "Lata", purchasePrice = Money(930.0), contentAmount = Quantity(1.0), usageUnit = "Lata", updatedAt = now),
            Ingredient(name = "Levadura", purchaseUnit = "Bolsa 500g", purchasePrice = Money(3800.0), contentAmount = Quantity(500.0), usageUnit = "g", updatedAt = now),
            Ingredient(name = "Aceite", purchaseUnit = "Botella 900cc", purchasePrice = Money(3000.0), contentAmount = Quantity(900.0), usageUnit = "cc", updatedAt = now),
            Ingredient(name = "Sal", purchaseUnit = "Paquete 500g", purchasePrice = Money(930.0), contentAmount = Quantity(500.0), usageUnit = "g", updatedAt = now)
        )
        ingredients.forEach { ingredientRepository.insert(it) }
    }

    private suspend fun seedRecipes() {
        val now = System.currentTimeMillis()
        val muzzarella = Recipe(name = "Muzzarela", parentRecipeId = null, createdAt = now, updatedAt = now)
        val muzzarellaId = recipeRepository.insert(muzzarella)

        val ingredientes = ingredientRepository.observeAll().first()

        val muzzarellaIngredients = listOf(
            RecipeIngredient(recipeId = muzzarellaId, ingredientId = ingredientes.first { it.name == "Harina" }.id, primaryMode = IngredientUsageMode.ByUsage(Quantity(0.3))),
            RecipeIngredient(recipeId = muzzarellaId, ingredientId = ingredientes.first { it.name == "Mozzarella" }.id, primaryMode = IngredientUsageMode.ByUsage(Quantity(0.25))),
            RecipeIngredient(recipeId = muzzarellaId, ingredientId = ingredientes.first { it.name == "Salsa de tomate" }.id, primaryMode = IngredientUsageMode.ByYield(6)),
            RecipeIngredient(recipeId = muzzarellaId, ingredientId = ingredientes.first { it.name == "Levadura" }.id, primaryMode = IngredientUsageMode.ByUsage(Quantity(6.667))),
            RecipeIngredient(recipeId = muzzarellaId, ingredientId = ingredientes.first { it.name == "Aceite" }.id, primaryMode = IngredientUsageMode.ByUsage(Quantity(8.333))),
            RecipeIngredient(recipeId = muzzarellaId, ingredientId = ingredientes.first { it.name == "Sal" }.id, primaryMode = IngredientUsageMode.ByUsage(Quantity(4.167)))
        )
        recipeRepository.setIngredients(muzzarellaId, muzzarellaIngredients)
    }

    private suspend fun seedAdditionalCosts() {
        additionalCostRepository.insert(
            AdditionalVariableCost(recipeId = null, concept = "Bandeja de telgopor", unitCost = Money(230.0), note = "1 unidad por pizza")
        )
    }

    private suspend fun seedSettings() {
        settingsRepository.updateSettings(
            AppSettings(
                batchSize = 6,
                wasteFactor = 0.0,
                discountPct = 0.0,
                estimatedMonthlyProduction = 500,
                targetMargin = 0.5,
                seedVersion = CURRENT_SEED_VERSION
            )
        )
    }
}
