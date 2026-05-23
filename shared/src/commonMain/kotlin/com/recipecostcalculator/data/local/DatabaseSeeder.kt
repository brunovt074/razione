package com.recipecostcalculator.data.local

import com.recipecostcalculator.domain.model.AdditionalVariableCost
import com.recipecostcalculator.domain.model.AppSettings
import com.recipecostcalculator.domain.model.Ingredient
import com.recipecostcalculator.domain.model.ByUsage
import com.recipecostcalculator.domain.model.ByYield
import com.recipecostcalculator.domain.model.IngredientUsageMode
import com.recipecostcalculator.domain.model.Recipe
import com.recipecostcalculator.domain.model.RecipeIngredient
import com.recipecostcalculator.domain.repository.AdditionalVariableCostRepository
import com.recipecostcalculator.domain.repository.IngredientRepository
import com.recipecostcalculator.domain.repository.RecipeRepository
import com.recipecostcalculator.domain.repository.SettingsRepository
import com.recipecostcalculator.financial.domain.model.Money
import com.recipecostcalculator.financial.domain.model.Quantity
import com.recipecostcalculator.measurement.MeasurementDimension
import com.recipecostcalculator.measurement.MeasurementUnit
import com.recipecostcalculator.measurement.UnitConverter
import kotlinx.coroutines.flow.first

class DatabaseSeeder(
    private val ingredientRepository: IngredientRepository,
    private val recipeRepository: RecipeRepository,
    private val additionalCostRepository: AdditionalVariableCostRepository,
    private val settingsRepository: SettingsRepository
) {
    companion object {
        private const val CURRENT_SEED_VERSION = 3
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

    private fun canonicalQuantity(amount: Double, inputUnit: MeasurementUnit): Quantity {
        val canonical = MeasurementUnit.canonicalFor(inputUnit.dimension)
        val canonicalValue = UnitConverter.toCanonical(amount, inputUnit)
        return Quantity(canonicalValue, canonical)
    }

    private suspend fun seedIngredients() {
        val now = System.currentTimeMillis()
        val ingredients = listOf(
            Ingredient(
                name = "Harina",
                dimension = MeasurementDimension.MASS,
                purchaseUnit = MeasurementUnit.KG,
                purchasePackageLabel = "Bolsa 25kg",
                purchasePrice = Money(18500.0),
                contentAmount = canonicalQuantity(25.0, MeasurementUnit.KG),
                usageUnit = MeasurementUnit.KG,
                updatedAt = now
            ),
            Ingredient(
                name = "Mozzarella",
                dimension = MeasurementDimension.MASS,
                purchaseUnit = MeasurementUnit.KG,
                purchasePrice = Money(9300.0),
                contentAmount = canonicalQuantity(1.0, MeasurementUnit.KG),
                usageUnit = MeasurementUnit.KG,
                updatedAt = now
            ),
            Ingredient(
                name = "Salsa de tomate",
                dimension = MeasurementDimension.COUNT,
                purchaseUnit = MeasurementUnit.UN,
                purchasePackageLabel = "Lata",
                purchasePrice = Money(930.0),
                contentAmount = canonicalQuantity(1.0, MeasurementUnit.UN),
                usageUnit = MeasurementUnit.UN,
                updatedAt = now
            ),
            Ingredient(
                name = "Levadura",
                dimension = MeasurementDimension.MASS,
                purchaseUnit = MeasurementUnit.G,
                purchasePackageLabel = "Bolsa 500g",
                purchasePrice = Money(3800.0),
                contentAmount = canonicalQuantity(500.0, MeasurementUnit.G),
                usageUnit = MeasurementUnit.G,
                updatedAt = now
            ),
            Ingredient(
                name = "Aceite",
                dimension = MeasurementDimension.VOLUME,
                purchaseUnit = MeasurementUnit.CC,
                purchasePackageLabel = "Botella 900cc",
                purchasePrice = Money(3000.0),
                contentAmount = canonicalQuantity(900.0, MeasurementUnit.CC),
                usageUnit = MeasurementUnit.CC,
                updatedAt = now
            ),
            Ingredient(
                name = "Sal",
                dimension = MeasurementDimension.MASS,
                purchaseUnit = MeasurementUnit.G,
                purchasePackageLabel = "Paquete 500g",
                purchasePrice = Money(930.0),
                contentAmount = canonicalQuantity(500.0, MeasurementUnit.G),
                usageUnit = MeasurementUnit.G,
                updatedAt = now
            )
        )
        ingredients.forEach { ingredientRepository.insert(it) }
    }

    private suspend fun seedRecipes() {
        val now = System.currentTimeMillis()
        val muzzarella = Recipe(name = "Muzzarela", parentRecipeId = null, createdAt = now, updatedAt = now)
        val muzzarellaId = recipeRepository.insert(muzzarella)

        val ingredientes = ingredientRepository.observeAll().first()

        val muzzarellaIngredients = listOf(
            RecipeIngredient(
                recipeId = muzzarellaId,
                ingredientId = ingredientes.first { it.name == "Harina" }.id,
                primaryMode = ByUsage(canonicalQuantity(0.3, MeasurementUnit.KG))
            ),
            RecipeIngredient(
                recipeId = muzzarellaId,
                ingredientId = ingredientes.first { it.name == "Mozzarella" }.id,
                primaryMode = ByUsage(canonicalQuantity(0.25, MeasurementUnit.KG))
            ),
            RecipeIngredient(
                recipeId = muzzarellaId,
                ingredientId = ingredientes.first { it.name == "Salsa de tomate" }.id,
                primaryMode = ByYield(6)
            ),
            RecipeIngredient(
                recipeId = muzzarellaId,
                ingredientId = ingredientes.first { it.name == "Levadura" }.id,
                primaryMode = ByUsage(canonicalQuantity(6.667, MeasurementUnit.G))
            ),
            RecipeIngredient(
                recipeId = muzzarellaId,
                ingredientId = ingredientes.first { it.name == "Aceite" }.id,
                primaryMode = ByUsage(canonicalQuantity(8.333, MeasurementUnit.CC))
            ),
            RecipeIngredient(
                recipeId = muzzarellaId,
                ingredientId = ingredientes.first { it.name == "Sal" }.id,
                primaryMode = ByUsage(canonicalQuantity(4.167, MeasurementUnit.G))
            )
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
