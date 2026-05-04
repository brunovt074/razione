package com.recipecostcalculator.domain.repository

import com.recipecostcalculator.domain.model.FixedCost
import com.recipecostcalculator.financial.domain.model.Money
import kotlinx.coroutines.flow.Flow

interface FixedCostRepository {
    fun observeAll(): Flow<List<FixedCost>>
    suspend fun getAll(): List<FixedCost>
    suspend fun insert(fixedCost: FixedCost): Long
    suspend fun update(fixedCost: FixedCost)
    suspend fun delete(id: Long)
    suspend fun getTotalMonthly(): Money
}