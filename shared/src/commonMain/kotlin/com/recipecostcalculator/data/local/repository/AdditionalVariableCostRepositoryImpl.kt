package com.recipecostcalculator.data.local.repository

import com.recipecostcalculator.db.PizzeriaDatabase
import com.recipecostcalculator.domain.model.AdditionalVariableCost
import com.recipecostcalculator.domain.repository.AdditionalVariableCostRepository
import com.recipecostcalculator.financial.domain.model.Money
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

class AdditionalVariableCostRepositoryImpl(
    private val db: PizzeriaDatabase
) : AdditionalVariableCostRepository {

    private val queries = db.additionalVariableCostsQueries

    override fun observeAll(): Flow<List<AdditionalVariableCost>> = flow {
        emit(getAll())
    }

    override suspend fun getAll(): List<AdditionalVariableCost> =
        withContext(Dispatchers.IO) {
            queries.selectAll().executeAsList().map { row ->
                AdditionalVariableCost(
                    id = row.id,
                    recipeId = row.recipe_id,
                    concept = row.concept,
                    unitCost = Money.of(row.unit_cost),
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
                    unitCost = Money.of(row.unit_cost),
                    note = row.note
                )
            }
        }

    override suspend fun insert(cost: AdditionalVariableCost): Long =
        withContext(Dispatchers.IO) {
            queries.insert(
                recipeId = cost.recipeId,
                concept = cost.concept,
                unitCost = cost.unitCost.toDouble(),
                note = cost.note
            )
            queries.lastInsertId().executeAsOne()
        }

    override suspend fun update(cost: AdditionalVariableCost): Unit =
        withContext(Dispatchers.IO) {
            queries.update(
                concept = cost.concept,
                unitCost = cost.unitCost.toDouble(),
                note = cost.note,
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