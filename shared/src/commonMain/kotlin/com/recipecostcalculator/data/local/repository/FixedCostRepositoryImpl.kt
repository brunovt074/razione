package com.recipecostcalculator.data.local.repository

import com.recipecostcalculator.db.PizzeriaDatabase
import com.recipecostcalculator.domain.model.FixedCost
import com.recipecostcalculator.domain.repository.FixedCostRepository
import com.recipecostcalculator.financial.domain.model.Money
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

class FixedCostRepositoryImpl(
    private val db: PizzeriaDatabase
) : FixedCostRepository {

    private val queries = db.fixedCostsQueries

    override fun observeAll(): Flow<List<FixedCost>> = flow {
        emit(getAll())
    }

    override suspend fun getTotalMonthly(): Money =
        withContext(Dispatchers.IO) {
            val total = queries.totalMonthly().executeAsOne()
            Money.of(total ?: 0.0)
        }

    override suspend fun getAll(): List<FixedCost> =
        withContext(Dispatchers.IO) {
            queries.selectAll().executeAsList().map { row ->
                FixedCost(
                    id = row.id,
                    concept = row.concept,
                    monthlyAmount = Money.of(row.monthly_amount)
                )
            }
        }

    override suspend fun insert(cost: FixedCost): Long =
        withContext(Dispatchers.IO) {
            queries.insert(
                concept = cost.concept,
                monthlyAmount = cost.monthlyAmount.toDouble()
            )
            queries.lastInsertId().executeAsOne()
        }

    override suspend fun update(cost: FixedCost): Unit =
        withContext(Dispatchers.IO) {
            queries.update(
                concept = cost.concept,
                monthlyAmount = cost.monthlyAmount.toDouble(),
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