package com.recipecostcalculator.data.local.repository

import com.recipecostcalculator.db.PizzeriaDatabase
import com.recipecostcalculator.domain.model.FixedCost
import com.recipecostcalculator.domain.repository.FixedCostRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class FixedCostRepositoryImpl(
    private val db: PizzeriaDatabase
) : FixedCostRepository {

    private val queries = db.fixedCostsQueries

    override fun observeAll(): Flow<List<FixedCost>> = kotlinx.coroutines.flow.flow {
        val list = queries.selectAll().executeAsList().map { row ->
            FixedCost(
                id = row.id,
                concept = row.concept,
                monthlyAmount = row.monthly_amount
            )
        }
        emit(list)
    }

    override suspend fun getTotalMonthly(): Double =
        withContext(Dispatchers.IO) {
            queries.totalMonthly().executeAsOne()
        }

    override suspend fun insert(cost: FixedCost): Long =
        withContext(Dispatchers.IO) {
            queries.insert(
                concept = cost.concept,
                monthlyAmount = cost.monthlyAmount
            )
            queries.lastInsertId().executeAsOne()
        }

    override suspend fun update(cost: FixedCost): Unit =
        withContext(Dispatchers.IO) {
            queries.update(
                concept = cost.concept,
                monthlyAmount = cost.monthlyAmount,
                id = cost.id
            )
            Unit
        }

    override suspend fun delete(id: Long): Unit =
        withContext(Dispatchers.IO) {
            queries.delete(id)
            Unit
        }
}