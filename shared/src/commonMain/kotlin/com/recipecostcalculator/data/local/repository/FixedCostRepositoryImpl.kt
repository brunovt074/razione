package com.recipecostcalculator.data.local.repository

import com.recipecostcalculator.db.PizzeriaDatabase
import com.recipecostcalculator.domain.model.FixedCost
import com.recipecostcalculator.domain.repository.FixedCostRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.withContext

class FixedCostRepositoryImpl(
    private val db: PizzeriaDatabase
) : FixedCostRepository {

    private val queries = db.fixedCostsQueries
    private val refreshSignal = MutableSharedFlow<Unit>(replay = 1)

    init {
        refreshSignal.tryEmit(Unit)
    }

    override fun observeAll(): Flow<List<FixedCost>> = refreshSignal.flatMapLatest {
        kotlinx.coroutines.flow.flow {
            val list = queries.selectAll().executeAsList().map { row ->
                FixedCost(
                    id = row.id,
                    concept = row.concept,
                    monthlyAmount = row.monthly_amount
                )
            }
            emit(list)
        }
    }

    private suspend fun refresh() {
        refreshSignal.emit(Unit)
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
            queries.lastInsertId().executeAsOne().also { refresh() }
        }

    override suspend fun update(cost: FixedCost): Unit =
        withContext(Dispatchers.IO) {
            queries.update(
                concept = cost.concept,
                monthlyAmount = cost.monthlyAmount,
                id = cost.id
            )
            refresh()
            Unit
        }

    override suspend fun delete(id: Long): Unit =
        withContext(Dispatchers.IO) {
            queries.delete(id)
            refresh()
            Unit
        }
}