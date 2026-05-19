package com.recipecostcalculator.domain.usecase

import com.recipecostcalculator.domain.model.AdditionalVariableCost
import com.recipecostcalculator.domain.model.AppSettings
import com.recipecostcalculator.domain.model.FixedCost
import com.recipecostcalculator.domain.model.Ingredient
import com.recipecostcalculator.domain.model.IngredientUsageMode
import com.recipecostcalculator.domain.model.Recipe
import com.recipecostcalculator.domain.model.RecipeIngredient
import com.recipecostcalculator.domain.model.SettingKey
import com.recipecostcalculator.domain.repository.AdditionalVariableCostRepository
import com.recipecostcalculator.domain.repository.FixedCostRepository
import com.recipecostcalculator.domain.repository.IngredientRepository
import com.recipecostcalculator.domain.repository.RecipeRepository
import com.recipecostcalculator.domain.repository.SettingsRepository
import com.recipecostcalculator.financial.domain.model.Money
import com.recipecostcalculator.financial.domain.model.Quantity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertTrue

class CalculateRecipeCostUseCaseTest {

    private val testRecipe = Recipe(
        id = 1L,
        name = "Muzzarela",
        parentRecipeId = null,
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
    )

    private val testIngredients = listOf(
        Ingredient(id = 1L, name = "Harina", purchaseUnit = "kg", purchasePrice = Money(1200.0), contentAmount = Quantity(1.0), usageUnit = "kg", isActive = true, updatedAt = System.currentTimeMillis()),
        Ingredient(id = 2L, name = "Mozzarella", purchaseUnit = "kg", purchasePrice = Money(4500.0), contentAmount = Quantity(1.0), usageUnit = "kg", isActive = true, updatedAt = System.currentTimeMillis()),
        Ingredient(id = 3L, name = "Salsa", purchaseUnit = "lt", purchasePrice = Money(1800.0), contentAmount = Quantity(1.0), usageUnit = "lt", isActive = true, updatedAt = System.currentTimeMillis())
    )

    private val testRecipeIngredients = listOf(
        RecipeIngredient(id = 1L, recipeId = 1L, ingredientId = 1L, primaryMode = IngredientUsageMode.ByUsage(Quantity(0.25))),
        RecipeIngredient(id = 2L, recipeId = 1L, ingredientId = 2L, primaryMode = IngredientUsageMode.ByUsage(Quantity(0.25))),
        RecipeIngredient(id = 3L, recipeId = 1L, ingredientId = 3L, primaryMode = IngredientUsageMode.ByUsage(Quantity(0.15)))
    )

    private val fakeRecipeRepository = object : RecipeRepository {
        override fun observeAll(): Flow<List<Recipe>> = flowOf(listOf(testRecipe))
        override suspend fun getAll(): List<Recipe> = listOf(testRecipe)
        override suspend fun getById(id: Long): Recipe? = testRecipe
        override suspend fun getBaseRecipes(): List<Recipe> = listOf(testRecipe)
        override suspend fun getByParent(parentId: Long): List<Recipe> = emptyList()
        override suspend fun insert(recipe: Recipe): Long = 1L
        override suspend fun update(recipe: Recipe) = Unit
        override suspend fun delete(id: Long) = Unit
        override suspend fun getIngredients(recipeId: Long): List<RecipeIngredient> = testRecipeIngredients
        override suspend fun setIngredients(recipeId: Long, ingredients: List<RecipeIngredient>) = Unit
        override suspend fun findAncestorChain(recipeId: Long, maxDepth: Int): List<Recipe> = listOf(testRecipe)
    }

    private val fakeIngredientRepository = object : IngredientRepository {
        override fun observeAll(): Flow<List<Ingredient>> = flowOf(testIngredients)
        override suspend fun getById(id: Long): Ingredient? = testIngredients.find { it.id == id }
        override suspend fun getAllActive(): List<Ingredient> = testIngredients
        override suspend fun insert(ingredient: Ingredient): Long = 1L
        override suspend fun update(ingredient: Ingredient) = Unit
        override suspend fun updatePrice(id: Long, newPrice: Money, updatedAt: Long) = Unit
        override suspend fun delete(id: Long) = Unit
    }

    private val fakeAdditionalCostRepository = object : AdditionalVariableCostRepository {
        override fun observeAll(): Flow<List<AdditionalVariableCost>> = flowOf(emptyList())
        override suspend fun getAll(): List<AdditionalVariableCost> = emptyList()
        override suspend fun getForRecipe(recipeId: Long): List<AdditionalVariableCost> = emptyList()
        override suspend fun insert(cost: AdditionalVariableCost): Long = 1L
        override suspend fun update(cost: AdditionalVariableCost) = Unit
        override suspend fun delete(id: Long) = Unit
    }

    private val fakeFixedCostRepository = object : FixedCostRepository {
        override fun observeAll(): Flow<List<FixedCost>> = flowOf(emptyList())
        override suspend fun getAll(): List<FixedCost> = emptyList()
        override suspend fun getTotalMonthly(): Money = Money.ZERO
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

        assertTrue(result.isSuccess)
    }

    @Test
    fun calculateCost_recipeWithParent_includesBothOwnAndParentIngredientCosts() = runBlocking {
        val muzzarela = Recipe(
            id = 1L,
            name = "Muzzarela",
            parentRecipeId = null,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        val fugazzeta = Recipe(
            id = 7L,
            name = "Fugazzeta",
            parentRecipeId = 1L,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        val allIngredients = listOf(
            Ingredient(id = 1L, name = "Harina", purchaseUnit = "kg", purchasePrice = Money(1200.0), contentAmount = Quantity(1.0), usageUnit = "kg"),
            Ingredient(id = 2L, name = "Mozzarella", purchaseUnit = "kg", purchasePrice = Money(4500.0), contentAmount = Quantity(1.0), usageUnit = "kg"),
            Ingredient(id = 3L, name = "Salsa", purchaseUnit = "lt", purchasePrice = Money(1800.0), contentAmount = Quantity(1.0), usageUnit = "lt"),
            Ingredient(id = 4L, name = "Cebolla", purchaseUnit = "kg", purchasePrice = Money(800.0), contentAmount = Quantity(1.0), usageUnit = "kg")
        )

        val muzzarelaIngredients = listOf(
            RecipeIngredient(id = 1L, recipeId = 1L, ingredientId = 1L, primaryMode = IngredientUsageMode.ByUsage(Quantity(0.25))),
            RecipeIngredient(id = 2L, recipeId = 1L, ingredientId = 2L, primaryMode = IngredientUsageMode.ByUsage(Quantity(0.25))),
            RecipeIngredient(id = 3L, recipeId = 1L, ingredientId = 3L, primaryMode = IngredientUsageMode.ByUsage(Quantity(0.15)))
        )

        val fugazzetaOwnIngredients = listOf(
            RecipeIngredient(id = 4L, recipeId = 7L, ingredientId = 4L, primaryMode = IngredientUsageMode.ByUsage(Quantity(0.1)))
        )

        val recipeRepository = object : RecipeRepository {
            private val recipes = mapOf(1L to muzzarela, 7L to fugazzeta)
            override fun observeAll(): Flow<List<Recipe>> = flowOf(recipes.values.toList())
            override suspend fun getAll(): List<Recipe> = recipes.values.toList()
            override suspend fun getById(id: Long): Recipe? = recipes[id]
            override suspend fun getBaseRecipes(): List<Recipe> = listOf(muzzarela)
            override suspend fun getByParent(parentId: Long): List<Recipe> = if (parentId == 1L) listOf(fugazzeta) else emptyList()
            override suspend fun insert(recipe: Recipe): Long = 7L
            override suspend fun update(recipe: Recipe) = Unit
            override suspend fun delete(id: Long) = Unit
            override suspend fun getIngredients(recipeId: Long): List<RecipeIngredient> = when (recipeId) {
                1L -> muzzarelaIngredients
                7L -> fugazzetaOwnIngredients
                else -> emptyList()
            }
            override suspend fun setIngredients(recipeId: Long, ingredients: List<RecipeIngredient>) = Unit
            override suspend fun findAncestorChain(recipeId: Long, maxDepth: Int): List<Recipe> = when (recipeId) {
                1L -> listOf(muzzarela)
                7L -> listOf(fugazzeta, muzzarela)
                else -> emptyList()
            }
        }

        val ingredientRepository = object : IngredientRepository {
            override fun observeAll(): Flow<List<Ingredient>> = flowOf(allIngredients)
            override suspend fun getById(id: Long): Ingredient? = allIngredients.find { it.id == id }
            override suspend fun getAllActive(): List<Ingredient> = allIngredients
            override suspend fun insert(ingredient: Ingredient): Long = ingredient.id
            override suspend fun update(ingredient: Ingredient) = Unit
            override suspend fun updatePrice(id: Long, newPrice: Money, updatedAt: Long) = Unit
            override suspend fun delete(id: Long) = Unit
        }

        val useCase = CalculateRecipeCostUseCase(
            recipeRepository = recipeRepository,
            ingredientRepository = ingredientRepository,
            additionalCostRepository = fakeAdditionalCostRepository,
            fixedCostRepository = fakeFixedCostRepository,
            settingsRepository = fakeSettingsRepository
        )

        val result = useCase(7L)

        assertTrue(result.isSuccess, "Expected success but got ${result.exceptionOrNull()?.message}")
        val breakdown = result.getOrThrow()

        assertTrue(breakdown.ingredientCostFromParent > Money.ZERO, "Parent ingredient cost should be > 0")
        assertTrue(breakdown.ingredientCostOwn > Money.ZERO, "Own ingredient cost should be > 0")
        assertTrue(breakdown.totalIngredientCost == breakdown.ingredientCostFromParent + breakdown.ingredientCostOwn,
            "Total ingredient cost should equal parent + own")
        assertTrue(breakdown.totalVariableCost == breakdown.totalIngredientCost,
            "Variable cost should equal ingredient cost (no additional costs in this test)")
    }
}
