package com.recipecostcalculator.domain.repository

import com.recipecostcalculator.domain.model.Recipe
import com.recipecostcalculator.domain.model.RecipeIngredient
import kotlinx.coroutines.flow.Flow

interface RecipeRepository {
    fun observeAll(): Flow<List<Recipe>>
    suspend fun getAll(): List<Recipe>
    suspend fun getById(id: Long): Recipe?
    suspend fun getBaseRecipes(): List<Recipe>
    suspend fun getAllForDashboard(): List<Recipe>
    suspend fun getByParent(parentId: Long): List<Recipe>
    suspend fun insert(recipe: Recipe): Long
    suspend fun update(recipe: Recipe)
    suspend fun delete(id: Long)
    suspend fun setIngredients(recipeId: Long, ingredients: List<RecipeIngredient>)
    suspend fun getIngredients(recipeId: Long): List<RecipeIngredient>
    suspend fun findAncestorChain(recipeId: Long, maxDepth: Int = 5): List<Recipe>
}