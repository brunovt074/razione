package com.recipecostcalculator.data.local.repository

import com.recipecostcalculator.db.PizzeriaDatabase
import com.recipecostcalculator.domain.model.IngredientUsageMode
import com.recipecostcalculator.domain.model.Recipe
import com.recipecostcalculator.domain.model.RecipeIngredient
import com.recipecostcalculator.domain.repository.RecipeRepository
import com.recipecostcalculator.financial.domain.model.Quantity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.withContext

class RecipeRepositoryImpl(
    private val db: PizzeriaDatabase
) : RecipeRepository {

    private val queries = db.recipesQueries
    private val recipeIngredientQueries = db.recipeIngredientsQueries
    private val refreshSignal = MutableSharedFlow<Unit>(replay = 1)

    init {
        refreshSignal.tryEmit(Unit)
    }

    override fun observeAll(): Flow<List<Recipe>> = refreshSignal.flatMapLatest {
        kotlinx.coroutines.flow.flow {
            val list = queries.selectAll().executeAsList().map { row ->
                Recipe(
                    id = row.id,
                    name = row.name,
                    parentRecipeId = row.parent_recipe_id,
                    recipeIngredients = loadRecipeIngredients(row.id),
                    createdAt = row.created_at,
                    updatedAt = row.updated_at
                )
            }
            emit(list)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun loadRecipeIngredients(recipeId: Long): List<RecipeIngredient> =
        recipeIngredientQueries.selectByRecipe(recipeId).executeAsList().map { row ->
            RecipeIngredient(
                id = row.id,
                recipeId = row.recipe_id,
                ingredientId = row.ingredient_id,
                primaryMode = rowToUsageMode(row.usage_per_pizza, row.yield_pizzas),
                ingredient = null
            )
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun rowToUsageMode(usagePerPizza: Double?, yieldPizzas: Long?): IngredientUsageMode {
        return when {
            usagePerPizza != null -> IngredientUsageMode.ByUsage(Quantity(usagePerPizza))
            yieldPizzas != null -> IngredientUsageMode.ByYield(yieldPizzas.toInt())
            else -> IngredientUsageMode.ByUsage(Quantity(0.0))
        }
    }

    private suspend fun refresh() {
        refreshSignal.emit(Unit)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun getById(id: Long): Recipe? =
        withContext(Dispatchers.IO) {
            queries.selectById(id).executeAsOneOrNull()?.let { row ->
                Recipe(
                    id = row.id,
                    name = row.name,
                    parentRecipeId = row.parent_recipe_id,
                    recipeIngredients = loadRecipeIngredients(row.id),
                    createdAt = row.created_at,
                    updatedAt = row.updated_at
                )
            }
        }

    override suspend fun getAll(): List<Recipe> =
        withContext(Dispatchers.IO) {
            queries.selectAll().executeAsList().map { row ->
                Recipe(
                    id = row.id,
                    name = row.name,
                    parentRecipeId = row.parent_recipe_id,
                    recipeIngredients = loadRecipeIngredients(row.id),
                    createdAt = row.created_at,
                    updatedAt = row.updated_at
                )
            }
        }

    override suspend fun getBaseRecipes(): List<Recipe> =
        withContext(Dispatchers.IO) {
            queries.selectBaseRecipes().executeAsList().map { row ->
                Recipe(
                    id = row.id,
                    name = row.name,
                    parentRecipeId = row.parent_recipe_id,
                    recipeIngredients = loadRecipeIngredients(row.id),
                    createdAt = row.created_at,
                    updatedAt = row.updated_at
                )
            }
        }

    override suspend fun getByParent(parentId: Long): List<Recipe> =
        withContext(Dispatchers.IO) {
            queries.selectByParent(parentId).executeAsList().map { row ->
                Recipe(
                    id = row.id,
                    name = row.name,
                    parentRecipeId = row.parent_recipe_id,
                    recipeIngredients = loadRecipeIngredients(row.id),
                    createdAt = row.created_at,
                    updatedAt = row.updated_at
                )
            }
        }

    override suspend fun insert(recipe: Recipe): Long =
        withContext(Dispatchers.IO) {
            queries.insert(
                name = recipe.name,
                parentRecipeId = recipe.parentRecipeId,
                createdAt = recipe.createdAt,
                updatedAt = recipe.updatedAt
            )
            queries.lastInsertId().executeAsOne().also { refresh() }
        }

    override suspend fun update(recipe: Recipe): Unit =
        withContext(Dispatchers.IO) {
            queries.update(
                name = recipe.name,
                parentRecipeId = recipe.parentRecipeId,
                updatedAt = System.currentTimeMillis(),
                id = recipe.id
            )
            refresh()
            Unit
        }

    override suspend fun delete(id: Long): Unit =
        withContext(Dispatchers.IO) {
            queries.delete(id)
            refresh()
            Unit
        }

    override suspend fun setIngredients(recipeId: Long, ingredients: List<RecipeIngredient>): Unit =
        withContext(Dispatchers.IO) {
            recipeIngredientQueries.deleteByRecipe(recipeId)
            ingredients.forEach { ri ->
                val (usagePerPizza, yieldPizzas) = usageModeToRowValues(ri.primaryMode)
                recipeIngredientQueries.insertOrReplace(
                    recipeId = recipeId,
                    ingredientId = ri.ingredientId,
                    usagePerPizza = usagePerPizza,
                    yieldPizzas = yieldPizzas
                )
            }
            refresh()
            Unit
        }

    override suspend fun getIngredients(recipeId: Long): List<RecipeIngredient> =
        withContext(Dispatchers.IO) {
            recipeIngredientQueries.selectByRecipe(recipeId).executeAsList().map { row ->
                RecipeIngredient(
                    id = row.id,
                    recipeId = row.recipe_id,
                    ingredientId = row.ingredient_id,
                    primaryMode = rowToUsageMode(row.usage_per_pizza, row.yield_pizzas),
                    ingredient = null
                )
            }
        }

    override suspend fun findAncestorChain(recipeId: Long, maxDepth: Int): List<Recipe> =
        withContext(Dispatchers.IO) {
            val chain = mutableListOf<Recipe>()
            var currentId: Long? = recipeId
            var depth = 0
            while (currentId != null && depth < maxDepth) {
                val recipe = queries.selectById(currentId).executeAsOneOrNull() ?: break
                chain.add(
                    Recipe(
                        id = recipe.id,
                        name = recipe.name,
                        parentRecipeId = recipe.parent_recipe_id,
                        recipeIngredients = loadRecipeIngredients(recipe.id),
                        createdAt = recipe.created_at,
                        updatedAt = recipe.updated_at
                    )
                )
                currentId = recipe.parent_recipe_id
                depth++
            }
            chain
        }

    private fun usageModeToRowValues(mode: IngredientUsageMode): Pair<Double?, Long?> {
        return when (mode) {
            is IngredientUsageMode.ByUsage -> Pair(mode.amountPerPizza.value, null)
            is IngredientUsageMode.ByYield -> Pair(null, mode.pizzasPerPurchaseUnit.toLong())
        }
    }
}
