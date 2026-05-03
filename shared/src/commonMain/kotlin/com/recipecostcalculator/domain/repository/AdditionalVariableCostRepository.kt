package com.recipecostcalculator.domain.repository

import com.recipecostcalculator.domain.model.AdditionalVariableCost
import kotlinx.coroutines.flow.Flow

interface AdditionalVariableCostRepository {
    fun observeAll(): Flow<List<AdditionalVariableCost>>
    suspend fun getForRecipe(recipeId: Long): List<AdditionalVariableCost>
    suspend fun insert(cost: AdditionalVariableCost): Long
    suspend fun update(cost: AdditionalVariableCost)
    suspend fun delete(id: Long)
}