package com.recipecostcalculator.data.local.repository

import com.recipecostcalculator.db.PizzeriaDatabase
import com.recipecostcalculator.domain.model.FixedCost
import com.recipecostcalculator.domain.repository.FixedCostRepository
import com.recipecostcalculator.financial.domain.model.Money
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeAll(): Flow<List<FixedCost>> = refreshSignal.flatMapLatest {
        kotlinx.coroutines.flow.flow {
            val list = queries.selectAll().executeAsList().map { row ->
                FixedCost(
                    id = row.id,
                    concept = row.concept,
                    monthlyAmount = Money(row.monthly_amount)
                )
            }
            emit(list)
        }
    }

    private suspend fun refresh() {
        refreshSignal.emit(Unit)
    }

    override suspend fun getAll(): List<FixedCost> =
        withContext(Dispatchers.IO) {
            queries.selectAll().executeAsList().map { row ->
                FixedCost(
                    id = row.id,
                    concept = row.concept,
                    monthlyAmount = Money(row.monthly_amount)
                )
            }
        }

    override suspend fun getTotalMonthly(): Money =
        withContext(Dispatchers.IO) {
            Money(queries.totalMonthly().executeAsOne())
        }

    override suspend fun insert(fixedCost: FixedCost): Long =
        withContext(Dispatchers.IO) {
            queries.insert(
                concept = fixedCost.concept,
                monthlyAmount = fixedCost.monthlyAmount.amount
            )
            queries.lastInsertId().executeAsOne().also { refresh() }
        }

    override suspend fun update(fixedCost: FixedCost): Unit =
        withContext(Dispatchers.IO) {
            queries.update(
                concept = fixedCost.concept,
                monthlyAmount = fixedCost.monthlyAmount.amount,
                id = fixedCost.id
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
