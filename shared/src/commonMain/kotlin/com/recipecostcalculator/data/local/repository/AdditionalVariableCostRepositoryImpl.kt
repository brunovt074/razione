package com.recipecostcalculator.data.local.repository

import com.recipecostcalculator.db.PizzeriaDatabase
import com.recipecostcalculator.domain.model.AdditionalVariableCost
import com.recipecostcalculator.domain.repository.AdditionalVariableCostRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.withContext

class AdditionalVariableCostRepositoryImpl(
    private val db: PizzeriaDatabase
) : AdditionalVariableCostRepository {

    private val queries = db.additionalVariableCostsQueries
    private val refreshSignal = MutableSharedFlow<Unit>(replay = 1)

    init {
        refreshSignal.tryEmit(Unit)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeAll(): Flow<List<AdditionalVariableCost>> = refreshSignal.flatMapLatest {
        kotlinx.coroutines.flow.flow {
            val list = queries.selectAll().executeAsList().map { row ->
                AdditionalVariableCost(
                    id = row.id,
                    recipeId = row.recipe_id,
                    concept = row.concept,
                    unitCost = com.recipecostcalculator.financial.domain.model.Money(row.unit_cost),
                    note = row.note
                )
            }
            emit(list)
        }
    }

    private suspend fun refresh() {
        refreshSignal.emit(Unit)
    }

    override suspend fun getAll(): List<AdditionalVariableCost> =
        withContext(Dispatchers.IO) {
            queries.selectAll().executeAsList().map { row ->
                AdditionalVariableCost(
                    id = row.id,
                    recipeId = row.recipe_id,
                    concept = row.concept,
                    unitCost = com.recipecostcalculator.financial.domain.model.Money(row.unit_cost),
                    note = row.note
                )
            }
        }

    override suspend fun getForRecipe(recipeId: Long): List<AdditionalVariableCost> =
        withContext(Dispatchers.IO) {
            queries.selectByRecipe(recipeId).executeAsList().map { row ->
                AdditionalVariableCost(
                    id = row.id,
                    recipeId = row.recipe_id,
                    concept = row.concept,
                    unitCost = com.recipecostcalculator.financial.domain.model.Money(row.unit_cost),
                    note = row.note
                )
            }
        }

    override suspend fun insert(cost: AdditionalVariableCost): Long =
        withContext(Dispatchers.IO) {
            queries.insert(
                recipeId = cost.recipeId,
                concept = cost.concept,
                unitCost = cost.unitCost.amount,
                note = cost.note
            )
            queries.lastInsertId().executeAsOne().also { refresh() }
        }

    override suspend fun update(cost: AdditionalVariableCost): Unit =
        withContext(Dispatchers.IO) {
            queries.update(
                concept = cost.concept,
                unitCost = cost.unitCost.amount,
                note = cost.note,
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
