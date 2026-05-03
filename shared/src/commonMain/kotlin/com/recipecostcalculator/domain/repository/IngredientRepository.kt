package com.recipecostcalculator.domain.repository

import com.recipecostcalculator.domain.model.Ingredient
import kotlinx.coroutines.flow.Flow

interface IngredientRepository {
    fun observeAll(): Flow<List<Ingredient>>
    suspend fun getById(id: Long): Ingredient?
    suspend fun insert(ingredient: Ingredient): Long
    suspend fun update(ingredient: Ingredient)
    suspend fun updatePrice(id: Long, newPrice: Double)
    suspend fun delete(id: Long)
}