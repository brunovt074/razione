package com.recipecostcalculator.fixedcost.domain.repository

import com.recipecostcalculator.fixedcost.domain.model.FixedCost
import kotlinx.coroutines.flow.Flow

interface FixedCostRepository {
    fun observeAll(): Flow<List<FixedCost>>
    suspend fun findById(id: Long): FixedCost?
    suspend fun getTotalMonthly(): Double
    suspend fun save(fixedCost: FixedCost): FixedCost
    suspend fun delete(id: Long)
}