package com.recipecostcalculator.domain.repository

import com.recipecostcalculator.domain.model.Ingredient
import com.recipecostcalculator.financial.domain.model.Money
import kotlinx.coroutines.flow.Flow

interface IngredientRepository {
    fun observeAll(): Flow<List<Ingredient>>
    suspend fun getById(id: Long): Ingredient?
    suspend fun getAllActive(): List<Ingredient>
    suspend fun insert(ingredient: Ingredient): Long
    suspend fun update(ingredient: Ingredient)
    suspend fun updatePrice(id: Long, newPrice: Money, updatedAt: Long)
    suspend fun delete(id: Long)
}