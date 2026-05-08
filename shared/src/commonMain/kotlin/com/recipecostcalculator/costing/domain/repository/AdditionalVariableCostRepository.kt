package com.recipecostcalculator.costing.domain.repository

import com.recipecostcalculator.costing.domain.model.AdditionalVariableCost
import kotlinx.coroutines.flow.Flow

interface AdditionalVariableCostRepository {
    fun observeAll(): Flow<List<AdditionalVariableCost>>
    suspend fun findById(id: Long): AdditionalVariableCost?
    suspend fun findForRecipe(recipeId: Long): List<AdditionalVariableCost>
    suspend fun save(additionalCost: AdditionalVariableCost): AdditionalVariableCost
    suspend fun delete(id: Long)
}