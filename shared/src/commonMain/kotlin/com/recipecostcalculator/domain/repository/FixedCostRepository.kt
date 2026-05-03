package com.recipecostcalculator.domain.repository

import com.recipecostcalculator.domain.model.FixedCost
import kotlinx.coroutines.flow.Flow

interface FixedCostRepository {
    fun observeAll(): Flow<List<FixedCost>>
    suspend fun getTotalMonthly(): Double
    suspend fun insert(cost: FixedCost): Long
    suspend fun update(cost: FixedCost)
    suspend fun delete(id: Long)
}