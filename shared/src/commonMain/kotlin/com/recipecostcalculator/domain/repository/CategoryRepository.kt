package com.recipecostcalculator.domain.repository

import com.recipecostcalculator.domain.model.Category
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun observeAll(): Flow<List<Category>>
    suspend fun getAll(): List<Category>
    suspend fun getAllWithCount(): List<Category>
    suspend fun getById(id: Long): Category?
    suspend fun insert(category: Category): Long
    suspend fun update(category: Category)
    suspend fun delete(id: Long)
    suspend fun countRecipes(categoryId: Long): Long
}
