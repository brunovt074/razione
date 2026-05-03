package com.recipecostcalculator.domain.usecase

import com.recipecostcalculator.domain.model.AdditionalVariableCost
import com.recipecostcalculator.domain.model.AppSettings
import com.recipecostcalculator.domain.model.FixedCost
import com.recipecostcalculator.domain.model.Ingredient
import com.recipecostcalculator.domain.model.Recipe
import com.recipecostcalculator.domain.model.RecipeIngredient
import com.recipecostcalculator.domain.model.SettingKey
import com.recipecostcalculator.domain.repository.AdditionalVariableCostRepository
import com.recipecostcalculator.domain.repository.FixedCostRepository
import com.recipecostcalculator.domain.repository.IngredientRepository
import com.recipecostcalculator.domain.repository.RecipeRepository
import com.recipecostcalculator.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals

class CalculateRecipeCostUseCaseTest {

    private val testRecipe = Recipe(
        id = 1L,
        name = "Muzzarela",
        parentRecipeId = null,
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
    )

    private val testIngredients = listOf(
        Ingredient(id = 1L, name = "Harina", purchaseUnit = "kg", purchasePrice = 1200.0, contentAmount = 1.0, usageUnit = "kg", isActive = true, updatedAt = System.currentTimeMillis()),
        Ingredient(id = 2L, name = "Mozzarella", purchaseUnit = "kg", purchasePrice = 4500.0, contentAmount = 1.0, usageUnit = "kg", isActive = true, updatedAt = System.currentTimeMillis()),
        Ingredient(id = 3L, name = "Salsa", purchaseUnit = "lt", purchasePrice = 1800.0, contentAmount = 1.0, usageUnit = "lt", isActive = true, updatedAt = System.currentTimeMillis())
    )

    private val testRecipeIngredients = listOf(
        RecipeIngredient(id = 1L, recipeId = 1L, ingredientId = 1L, usagePerPizza = 0.25, yieldPizzas = null),
        RecipeIngredient(id = 2L, recipeId = 1L, ingredientId = 2L, usagePerPizza = 0.25, yieldPizzas = null),
        RecipeIngredient(id = 3L, recipeId = 1L, ingredientId = 3L, usagePerPizza = 0.15, yieldPizzas = null)
    )

    private val fakeRecipeRepository = object : RecipeRepository {
        override fun observeAll(): Flow<List<Recipe>> = flowOf(listOf(testRecipe))
        override suspend fun getById(id: Long): Recipe? = testRecipe
        override suspend fun getBaseRecipes(): List<Recipe> = listOf(testRecipe)
        override suspend fun getByParent(parentId: Long): List<Recipe> = emptyList()
        override suspend fun insert(recipe: Recipe): Long = 1L
        override suspend fun update(recipe: Recipe) = Unit
        override suspend fun delete(id: Long) = Unit
        override suspend fun getIngredients(recipeId: Long): List<RecipeIngredient> = testRecipeIngredients
        override suspend fun setIngredients(recipeId: Long, ingredients: List<RecipeIngredient>) = Unit
    }

    private val fakeIngredientRepository = object : IngredientRepository {
        override fun observeAll(): Flow<List<Ingredient>> = flowOf(testIngredients)
        override suspend fun getById(id: Long): Ingredient? = testIngredients.find { it.id == id }
        override suspend fun insert(ingredient: Ingredient): Long = 1L
        override suspend fun update(ingredient: Ingredient) = Unit
        override suspend fun updatePrice(id: Long, newPrice: Double) = Unit
        override suspend fun delete(id: Long) = Unit
    }

    private val fakeAdditionalCostRepository = object : AdditionalVariableCostRepository {
        override fun observeAll(): Flow<List<AdditionalVariableCost>> = flowOf(emptyList())
        override suspend fun getForRecipe(recipeId: Long): List<AdditionalVariableCost> = emptyList()
        override suspend fun insert(cost: AdditionalVariableCost): Long = 1L
        override suspend fun update(cost: AdditionalVariableCost) = Unit
        override suspend fun delete(id: Long) = Unit
    }

    private val fakeFixedCostRepository = object : FixedCostRepository {
        override fun observeAll(): Flow<List<FixedCost>> = flowOf(emptyList())
        override suspend fun getTotalMonthly(): Double = 0.0
        override suspend fun insert(fixedCost: FixedCost): Long = 1L
        override suspend fun update(fixedCost: FixedCost) = Unit
        override suspend fun delete(id: Long) = Unit
    }

    private val fakeSettingsRepository = object : SettingsRepository {
        override fun observeSettings(): Flow<AppSettings> = flowOf(AppSettings())
        override suspend fun getSettings(): AppSettings = AppSettings()
        override suspend fun updateSetting(key: SettingKey, value: String) = Unit
        override suspend fun updateSettings(settings: AppSettings) = Unit
    }

    @Test
    fun calculateCost_returnsSuccessResult() = runBlocking {
        val useCase = CalculateRecipeCostUseCase(
            recipeRepository = fakeRecipeRepository,
            ingredientRepository = fakeIngredientRepository,
            additionalCostRepository = fakeAdditionalCostRepository,
            fixedCostRepository = fakeFixedCostRepository,
            settingsRepository = fakeSettingsRepository
        )

        val result = useCase(1L)

        assertTrue("Result should be success", result.isSuccess)
    }
}