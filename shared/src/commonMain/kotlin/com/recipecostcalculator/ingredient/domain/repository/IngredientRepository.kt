package com.recipecostcalculator.ingredient.domain.repository

import com.recipecostcalculator.financial.domain.model.Money
import com.recipecostcalculator.ingredient.domain.model.Ingredient
import kotlinx.coroutines.flow.Flow

interface IngredientRepository {
    fun observeAll(): Flow<List<Ingredient>>
    suspend fun findById(id: Long): Ingredient?
    suspend fun findAllActive(): List<Ingredient>
    suspend fun save(ingredient: Ingredient): Ingredient
    suspend fun updatePrice(id: Long, newPrice: Money, updatedAt: Long): Ingredient
    suspend fun deactivate(id: Long)
}