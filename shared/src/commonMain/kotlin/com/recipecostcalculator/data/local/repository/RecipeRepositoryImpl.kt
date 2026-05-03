package com.recipecostcalculator.data.local.repository

import com.recipecostcalculator.db.PizzeriaDatabase
import com.recipecostcalculator.domain.model.Recipe
import com.recipecostcalculator.domain.model.RecipeIngredient
import com.recipecostcalculator.domain.repository.RecipeRepository
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
                    recipeIngredients = emptyList(),
                    createdAt = row.created_at,
                    updatedAt = row.updated_at
                )
            }
            emit(list)
        }
    }

    private suspend fun refresh() {
        refreshSignal.emit(Unit)
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
                recipeIngredientQueries.insertOrReplace(
                    recipeId = recipeId,
                    ingredientId = ri.ingredientId,
                    usagePerPizza = ri.usagePerPizza,
                    yieldPizzas = ri.yieldPizzas
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
                    ingredient = null,
                    usagePerPizza = row.usage_per_pizza,
                    yieldPizzas = row.yield_pizzas
                )
            }
        }
}