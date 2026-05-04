package com.recipecostcalculator.data.local.repository

import com.recipecostcalculator.db.PizzeriaDatabase
import com.recipecostcalculator.domain.model.IngredientUsageMode
import com.recipecostcalculator.domain.model.Recipe
import com.recipecostcalculator.domain.model.RecipeIngredient
import com.recipecostcalculator.domain.repository.RecipeRepository
import com.recipecostcalculator.financial.domain.model.Quantity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

class RecipeRepositoryImpl(
    private val db: PizzeriaDatabase
) : RecipeRepository {

    private val queries = db.recipesQueries
    private val recipeIngredientQueries = db.recipeIngredientsQueries

    override fun observeAll(): Flow<List<Recipe>> = flow {
        emit(getAll())
    }

    override suspend fun getAll(): List<Recipe> =
        withContext(Dispatchers.IO) {
            queries.selectAll().executeAsList().map { row ->
                Recipe(
                    id = row.id,
                    name = row.name,
                    parentRecipeId = row.parent_recipe_id,
                    recipeIngredients = emptyList(),
                    createdAt = row.created_at,
                    updatedAt = row.updated_at
                )
            }
        }

    override suspend fun getById(id: Long): Recipe? =
        withContext(Dispatchers.IO) {
            queries.selectById(id).executeAsOneOrNull()?.let { row ->
                Recipe(
                    id = row.id,
                    name = row.name,
                    parentRecipeId = row.parent_recipe_id,
                    recipeIngredients = emptyList(),
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
                    recipeIngredients = emptyList(),
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
                    recipeIngredients = emptyList(),
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
            queries.lastInsertId().executeAsOne()
        }

    override suspend fun update(recipe: Recipe): Unit =
        withContext(Dispatchers.IO) {
            queries.update(
                name = recipe.name,
                parentRecipeId = recipe.parentRecipeId,
                updatedAt = System.currentTimeMillis(),
                id = recipe.id
            )
            Unit
        }

    override suspend fun delete(id: Long): Unit =
        withContext(Dispatchers.IO) {
            queries.delete(id)
            Unit
        }

    override suspend fun setIngredients(recipeId: Long, ingredients: List<RecipeIngredient>): Unit =
        withContext(Dispatchers.IO) {
            recipeIngredientQueries.deleteByRecipe(recipeId)
            ingredients.forEach { ri ->
                val (usagePerPizza, yieldPizzas) = when (val mode = ri.primaryMode) {
                    is IngredientUsageMode.ByUsage -> mode.amountPerPizza.value.toDouble() to null
                    is IngredientUsageMode.ByYield -> null to mode.pizzasPerPurchaseUnit.toLong()
                }
                recipeIngredientQueries.insertOrReplace(
                    recipeId = recipeId,
                    ingredientId = ri.ingredientId,
                    usagePerPizza = usagePerPizza,
                    yieldPizzas = yieldPizzas
                )
            }
            Unit
        }

    override suspend fun getIngredients(recipeId: Long): List<RecipeIngredient> =
        withContext(Dispatchers.IO) {
            recipeIngredientQueries.selectByRecipe(recipeId).executeAsList().map { row ->
                val mode = when {
                    row.usage_per_pizza != null ->
                        IngredientUsageMode.ByUsage(Quantity.of(row.usage_per_pizza))
                    row.yield_pizzas != null ->
                        IngredientUsageMode.ByYield(row.yield_pizzas.toInt())
                    else -> IngredientUsageMode.ByUsage(Quantity.of(0.0))
                }
                RecipeIngredient(
                    id = row.id,
                    recipeId = row.recipe_id,
                    ingredientId = row.ingredient_id,
                    primaryMode = mode,
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
                val recipe = queries.selectById(currentId).executeAsOneOrNull()
                    ?: break
                chain.add(
                    Recipe(
                        id = recipe.id,
                        name = recipe.name,
                        parentRecipeId = recipe.parent_recipe_id,
                        recipeIngredients = emptyList(),
                        createdAt = recipe.created_at,
                        updatedAt = recipe.updated_at
                    )
                )
                currentId = recipe.parent_recipe_id
                depth++
            }
            chain
        }
}