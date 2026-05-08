package com.recipecostcalculator.recipe.domain.repository

import com.recipecostcalculator.recipe.domain.model.Recipe
import kotlinx.coroutines.flow.Flow

interface RecipeRepository {
    fun observeAll(): Flow<List<Recipe>>
    suspend fun findById(id: Long): Recipe?
    suspend fun findBaseRecipes(): List<Recipe>
    suspend fun findByParent(parentId: Long): List<Recipe>
    suspend fun save(recipe: Recipe): Recipe
    suspend fun delete(id: Long)
    suspend fun findAncestorChain(recipeId: Long, maxDepth: Int = 5): List<Recipe>
}