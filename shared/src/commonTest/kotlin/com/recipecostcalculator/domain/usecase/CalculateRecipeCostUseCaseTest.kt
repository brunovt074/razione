package com.recipecostcalculator.domain.usecase

import com.recipecostcalculator.domain.model.AdditionalVariableCost
import com.recipecostcalculator.domain.model.AppSettings
import com.recipecostcalculator.domain.model.FixedCost
import com.recipecostcalculator.domain.model.Ingredient
import com.recipecostcalculator.domain.model.ByUsage
import com.recipecostcalculator.domain.model.ByYield
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
import com.recipecostcalculator.measurement.MeasurementDimension
import com.recipecostcalculator.measurement.MeasurementUnit
import com.recipecostcalculator.measurement.UnitConverter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CalculateRecipeCostUseCaseTest {

    private fun massQty(amount: Double, unit: MeasurementUnit): Quantity {
        val canonicalValue = UnitConverter.toCanonical(amount, unit)
        return Quantity(canonicalValue, MeasurementUnit.KG)
    }

    private fun volumeQty(amount: Double, unit: MeasurementUnit): Quantity {
        val canonicalValue = UnitConverter.toCanonical(amount, unit)
        return Quantity(canonicalValue, MeasurementUnit.L)
    }

    private fun countQty(amount: Double): Quantity = Quantity(amount, MeasurementUnit.UN)

    private val testRecipe = Recipe(
        id = 1L,
        name = "Muzzarela",
        parentRecipeId = null,
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
    )

    private val testIngredients = listOf(
        Ingredient(
            id = 1L, name = "Harina",
            dimension = MeasurementDimension.MASS,
            purchaseUnit = MeasurementUnit.KG,
            purchasePrice = Money(1200.0),
            contentAmount = massQty(1.0, MeasurementUnit.KG),
            usageUnit = MeasurementUnit.KG
        ),
        Ingredient(
            id = 2L, name = "Mozzarella",
            dimension = MeasurementDimension.MASS,
            purchaseUnit = MeasurementUnit.KG,
            purchasePrice = Money(4500.0),
            contentAmount = massQty(1.0, MeasurementUnit.KG),
            usageUnit = MeasurementUnit.KG
        ),
        Ingredient(
            id = 3L, name = "Salsa",
            dimension = MeasurementDimension.COUNT,
            purchaseUnit = MeasurementUnit.UN,
            purchasePrice = Money(1800.0),
            contentAmount = countQty(1.0),
            usageUnit = MeasurementUnit.UN
        )
    )

    private val testRecipeIngredients = listOf(
        RecipeIngredient(id = 1L, recipeId = 1L, ingredientId = 1L, primaryMode = ByUsage(massQty(0.25, MeasurementUnit.KG))),
        RecipeIngredient(id = 2L, recipeId = 1L, ingredientId = 2L, primaryMode = ByUsage(massQty(0.25, MeasurementUnit.KG))),
        RecipeIngredient(id = 3L, recipeId = 1L, ingredientId = 3L, primaryMode = ByUsage(countQty(0.15)))
    )

    private val fakeRecipeRepository = object : RecipeRepository {
        override fun observeAll(): Flow<List<Recipe>> = flowOf(listOf(testRecipe))
        override suspend fun getAll(): List<Recipe> = listOf(testRecipe)
        override suspend fun getById(id: Long): Recipe? = testRecipe
        override suspend fun getBaseRecipes(): List<Recipe> = listOf(testRecipe)
        override suspend fun getAllForDashboard(): List<Recipe> = listOf(testRecipe)
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
    fun calculateCost_groundTruth_muzzarela() = runBlocking {
        val harina = Ingredient(
            id = 1L, name = "Harina",
            dimension = MeasurementDimension.MASS,
            purchaseUnit = MeasurementUnit.KG,
            details = "Bolsa 25kg",
            purchasePrice = Money(18500.0),
            contentAmount = massQty(25.0, MeasurementUnit.KG),
            usageUnit = MeasurementUnit.KG
        )
        val mozzarella = Ingredient(
            id = 2L, name = "Mozzarella",
            dimension = MeasurementDimension.MASS,
            purchaseUnit = MeasurementUnit.KG,
            purchasePrice = Money(9300.0),
            contentAmount = massQty(1.0, MeasurementUnit.KG),
            usageUnit = MeasurementUnit.KG
        )
        val salsa = Ingredient(
            id = 3L, name = "Salsa",
            dimension = MeasurementDimension.COUNT,
            purchaseUnit = MeasurementUnit.UN,
            details = "Lata",
            purchasePrice = Money(930.0),
            contentAmount = countQty(1.0),
            usageUnit = MeasurementUnit.UN
        )
        val levadura = Ingredient(
            id = 4L, name = "Levadura",
            dimension = MeasurementDimension.MASS,
            purchaseUnit = MeasurementUnit.G,
            details = "Bolsa 500g",
            purchasePrice = Money(3800.0),
            contentAmount = massQty(500.0, MeasurementUnit.G),
            usageUnit = MeasurementUnit.G
        )
        val aceite = Ingredient(
            id = 5L, name = "Aceite",
            dimension = MeasurementDimension.VOLUME,
            purchaseUnit = MeasurementUnit.CC,
            details = "Botella 900cc",
            purchasePrice = Money(3000.0),
            contentAmount = volumeQty(900.0, MeasurementUnit.CC),
            usageUnit = MeasurementUnit.CC
        )
        val sal = Ingredient(
            id = 6L, name = "Sal",
            dimension = MeasurementDimension.MASS,
            purchaseUnit = MeasurementUnit.G,
            details = "Paquete 500g",
            purchasePrice = Money(930.0),
            contentAmount = massQty(500.0, MeasurementUnit.G),
            usageUnit = MeasurementUnit.G
        )
        val allIngredients = listOf(harina, mozzarella, salsa, levadura, aceite, sal)
        val bandeja = AdditionalVariableCost(recipeId = null, concept = "Bandeja", unitCost = Money(230.0), note = "")

        val muzzarela = Recipe(id = 1L, name = "Muzzarela", parentRecipeId = null, createdAt = 0, updatedAt = 0)
        val muzzarelaIngredients = listOf(
            RecipeIngredient(recipeId = 1L, ingredientId = 1L, primaryMode = ByUsage(massQty(0.3, MeasurementUnit.KG))),
            RecipeIngredient(recipeId = 1L, ingredientId = 2L, primaryMode = ByUsage(massQty(0.25, MeasurementUnit.KG))),
            RecipeIngredient(recipeId = 1L, ingredientId = 3L, primaryMode = ByYield(6)),
            RecipeIngredient(recipeId = 1L, ingredientId = 4L, primaryMode = ByUsage(massQty(6.667, MeasurementUnit.G))),
            RecipeIngredient(recipeId = 1L, ingredientId = 5L, primaryMode = ByUsage(volumeQty(8.333, MeasurementUnit.CC))),
            RecipeIngredient(recipeId = 1L, ingredientId = 6L, primaryMode = ByUsage(massQty(4.167, MeasurementUnit.G)))
        )

        val repo = object : RecipeRepository {
            override fun observeAll() = flowOf(listOf(muzzarela))
            override suspend fun getAll() = listOf(muzzarela)
            override suspend fun getById(id: Long) = if (id == 1L) muzzarela else null
            override suspend fun getBaseRecipes() = listOf(muzzarela)
            override suspend fun getAllForDashboard() = listOf(muzzarela)
            override suspend fun getByParent(parentId: Long) = emptyList<Recipe>()
            override suspend fun insert(recipe: Recipe) = 1L
            override suspend fun update(recipe: Recipe) = Unit
            override suspend fun delete(id: Long) = Unit
            override suspend fun getIngredients(recipeId: Long) = if (recipeId == 1L) muzzarelaIngredients else emptyList()
            override suspend fun setIngredients(recipeId: Long, ingredients: List<RecipeIngredient>) = Unit
            override suspend fun findAncestorChain(recipeId: Long, maxDepth: Int) = listOf(muzzarela)
        }
        val ingRepo = object : IngredientRepository {
            override fun observeAll() = flowOf(allIngredients)
            override suspend fun getById(id: Long) = allIngredients.find { it.id == id }
            override suspend fun getAllActive() = allIngredients
            override suspend fun insert(ingredient: Ingredient) = ingredient.id
            override suspend fun update(ingredient: Ingredient) = Unit
            override suspend fun updatePrice(id: Long, newPrice: Money, updatedAt: Long) = Unit
            override suspend fun delete(id: Long) = Unit
        }
        val addRepo = object : AdditionalVariableCostRepository {
            override fun observeAll() = flowOf(listOf(bandeja))
            override suspend fun getAll() = listOf(bandeja)
            override suspend fun getForRecipe(recipeId: Long) = listOf(bandeja)
            override suspend fun insert(cost: AdditionalVariableCost) = 1L
            override suspend fun update(cost: AdditionalVariableCost) = Unit
            override suspend fun delete(id: Long) = Unit
        }

        val useCase = CalculateRecipeCostUseCase(
            recipeRepository = repo,
            ingredientRepository = ingRepo,
            additionalCostRepository = addRepo,
            fixedCostRepository = fakeFixedCostRepository,
            settingsRepository = fakeSettingsRepository
        )

        val result = useCase(1L)
        assertTrue(result.isSuccess, "Expected success: ${result.exceptionOrNull()?.message}")
        val breakdown = result.getOrThrow()
        assertEquals(3018.17, breakdown.totalVariableCost.amount, 0.1)
    }

    @Test
    fun calculateCost_groundTruth_fugazzeta_cebolla200g_should_be_100() = runBlocking {
        val cebolla = Ingredient(
            id = 10L, name = "Cebolla",
            dimension = MeasurementDimension.MASS,
            purchaseUnit = MeasurementUnit.KG,
            purchasePrice = Money(10000.0),
            contentAmount = massQty(20.0, MeasurementUnit.KG),
            usageUnit = MeasurementUnit.KG
        )

        assertEquals(500.0, cebolla.unitCost().amount, 0.01)

        val muzzarela = Recipe(id = 1L, name = "Muzzarela", parentRecipeId = null, createdAt = 0, updatedAt = 0)
        val fugazzeta = Recipe(id = 2L, name = "Fugazzeta", parentRecipeId = 1L, createdAt = 0, updatedAt = 0)

        val fugazzetaIngredients = listOf(
            RecipeIngredient(
                recipeId = 2L, ingredientId = 10L,
                primaryMode = ByUsage(massQty(200.0, MeasurementUnit.G))
            )
        )
        val muzzarelaIngredients = emptyList<RecipeIngredient>()

        val repo = object : RecipeRepository {
            override fun observeAll() = flowOf(listOf(muzzarela, fugazzeta))
            override suspend fun getAll() = listOf(muzzarela, fugazzeta)
            override suspend fun getById(id: Long) = when (id) { 1L -> muzzarela; 2L -> fugazzeta; else -> null }
            override suspend fun getBaseRecipes() = listOf(muzzarela)
            override suspend fun getAllForDashboard() = listOf(muzzarela, fugazzeta)
            override suspend fun getByParent(parentId: Long) = if (parentId == 1L) listOf(fugazzeta) else emptyList()
            override suspend fun insert(recipe: Recipe) = recipe.id
            override suspend fun update(recipe: Recipe) = Unit
            override suspend fun delete(id: Long) = Unit
            override suspend fun getIngredients(recipeId: Long) = when (recipeId) { 2L -> fugazzetaIngredients; else -> muzzarelaIngredients }
            override suspend fun setIngredients(recipeId: Long, ingredients: List<RecipeIngredient>) = Unit
            override suspend fun findAncestorChain(recipeId: Long, maxDepth: Int) = when (recipeId) {
                2L -> listOf(fugazzeta, muzzarela)
                else -> listOf(muzzarela)
            }
        }
        val ingRepo = object : IngredientRepository {
            override fun observeAll() = flowOf(listOf(cebolla))
            override suspend fun getById(id: Long) = if (id == 10L) cebolla else null
            override suspend fun getAllActive() = listOf(cebolla)
            override suspend fun insert(ingredient: Ingredient) = ingredient.id
            override suspend fun update(ingredient: Ingredient) = Unit
            override suspend fun updatePrice(id: Long, newPrice: Money, updatedAt: Long) = Unit
            override suspend fun delete(id: Long) = Unit
        }

        val useCase = CalculateRecipeCostUseCase(
            recipeRepository = repo,
            ingredientRepository = ingRepo,
            additionalCostRepository = fakeAdditionalCostRepository,
            fixedCostRepository = fakeFixedCostRepository,
            settingsRepository = fakeSettingsRepository
        )

        val result = useCase(2L)
        assertTrue(result.isSuccess, "Expected success: ${result.exceptionOrNull()?.message}")
        val breakdown = result.getOrThrow()

        val cebollaLine = breakdown.ingredientBreakdown.firstOrNull { it.ingredientName == "Cebolla" }
        assertTrue(cebollaLine != null, "Cebolla line not found")
        assertEquals(100.0, cebollaLine!!.costPerPizza.amount, 0.01)
    }

    @Test
    fun calculateCost_recipeWithParent_includesBothOwnAndParentIngredientCosts() = runBlocking {
        val muzzarela = Recipe(id = 1L, name = "Muzzarela", parentRecipeId = null, createdAt = 0, updatedAt = 0)
        val fugazzeta = Recipe(id = 7L, name = "Fugazzeta", parentRecipeId = 1L, createdAt = 0, updatedAt = 0)

        val allIngredients = listOf(
            Ingredient(id = 1L, name = "Harina", dimension = MeasurementDimension.MASS, purchaseUnit = MeasurementUnit.KG, purchasePrice = Money(1200.0), contentAmount = massQty(1.0, MeasurementUnit.KG), usageUnit = MeasurementUnit.KG),
            Ingredient(id = 2L, name = "Mozzarella", dimension = MeasurementDimension.MASS, purchaseUnit = MeasurementUnit.KG, purchasePrice = Money(4500.0), contentAmount = massQty(1.0, MeasurementUnit.KG), usageUnit = MeasurementUnit.KG),
            Ingredient(id = 3L, name = "Salsa", dimension = MeasurementDimension.COUNT, purchaseUnit = MeasurementUnit.UN, purchasePrice = Money(1800.0), contentAmount = countQty(1.0), usageUnit = MeasurementUnit.UN),
            Ingredient(id = 4L, name = "Cebolla", dimension = MeasurementDimension.MASS, purchaseUnit = MeasurementUnit.KG, purchasePrice = Money(800.0), contentAmount = massQty(1.0, MeasurementUnit.KG), usageUnit = MeasurementUnit.KG)
        )

        val muzzarelaIngredients = listOf(
            RecipeIngredient(id = 1L, recipeId = 1L, ingredientId = 1L, primaryMode = ByUsage(massQty(0.25, MeasurementUnit.KG))),
            RecipeIngredient(id = 2L, recipeId = 1L, ingredientId = 2L, primaryMode = ByUsage(massQty(0.25, MeasurementUnit.KG))),
            RecipeIngredient(id = 3L, recipeId = 1L, ingredientId = 3L, primaryMode = ByUsage(countQty(0.15)))
        )

        val fugazzetaOwnIngredients = listOf(
            RecipeIngredient(id = 4L, recipeId = 7L, ingredientId = 4L, primaryMode = ByUsage(massQty(0.1, MeasurementUnit.KG)))
        )

        val recipeRepository = object : RecipeRepository {
            private val recipes = mapOf(1L to muzzarela, 7L to fugazzeta)
            override fun observeAll() = flowOf(recipes.values.toList())
            override suspend fun getAll() = recipes.values.toList()
            override suspend fun getById(id: Long) = recipes[id]
            override suspend fun getBaseRecipes() = listOf(muzzarela)
            override suspend fun getAllForDashboard() = listOf(muzzarela, fugazzeta)
            override suspend fun getByParent(parentId: Long) = if (parentId == 1L) listOf(fugazzeta) else emptyList()
            override suspend fun insert(recipe: Recipe) = 7L
            override suspend fun update(recipe: Recipe) = Unit
            override suspend fun delete(id: Long) = Unit
            override suspend fun getIngredients(recipeId: Long) = when (recipeId) { 1L -> muzzarelaIngredients; 7L -> fugazzetaOwnIngredients; else -> emptyList() }
            override suspend fun setIngredients(recipeId: Long, ingredients: List<RecipeIngredient>) = Unit
            override suspend fun findAncestorChain(recipeId: Long, maxDepth: Int) = when (recipeId) { 1L -> listOf(muzzarela); 7L -> listOf(fugazzeta, muzzarela); else -> emptyList() }
        }

        val ingredientRepository = object : IngredientRepository {
            override fun observeAll() = flowOf(allIngredients)
            override suspend fun getById(id: Long) = allIngredients.find { it.id == id }
            override suspend fun getAllActive() = allIngredients
            override suspend fun insert(ingredient: Ingredient) = ingredient.id
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
    }
}
