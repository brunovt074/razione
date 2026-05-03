package com.recipecostcalculator.data.local.repository

import com.recipecostcalculator.db.PizzeriaDatabase
import com.recipecostcalculator.domain.model.Ingredient
import com.recipecostcalculator.domain.repository.IngredientRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.withContext

class IngredientRepositoryImpl(
    private val db: PizzeriaDatabase
) : IngredientRepository {

    private val queries = db.ingredientsQueries
    private val refreshSignal = MutableSharedFlow<Unit>(replay = 1)

    init {
        refreshSignal.tryEmit(Unit)
    }

    override fun observeAll(): Flow<List<Ingredient>> = refreshSignal.flatMapLatest {
        kotlinx.coroutines.flow.flow {
            val list = queries.selectAll().executeAsList().map { row ->
                Ingredient(
                    id = row.id,
                    name = row.name,
                    purchaseUnit = row.purchase_unit,
                    purchasePrice = row.purchase_price,
                    contentAmount = row.content_amount,
                    usageUnit = row.usage_unit,
                    isActive = row.is_active == 1L,
                    updatedAt = row.updated_at
                )
            }
            emit(list)
        }
    }

    private suspend fun refresh() {
        refreshSignal.emit(Unit)
    }

    override suspend fun getById(id: Long): Ingredient? =
        withContext(Dispatchers.IO) {
            queries.selectById(id).executeAsOneOrNull()?.let { row ->
                Ingredient(
                    id = row.id,
                    name = row.name,
                    purchaseUnit = row.purchase_unit,
                    purchasePrice = row.purchase_price,
                    contentAmount = row.content_amount,
                    usageUnit = row.usage_unit,
                    isActive = row.is_active == 1L,
                    updatedAt = row.updated_at
                )
            }
        }

    override suspend fun insert(ingredient: Ingredient): Long =
        withContext(Dispatchers.IO) {
            queries.insert(
                name = ingredient.name,
                purchaseUnit = ingredient.purchaseUnit,
                purchasePrice = ingredient.purchasePrice,
                contentAmount = ingredient.contentAmount,
                usageUnit = ingredient.usageUnit,
                updatedAt = ingredient.updatedAt
            )
            queries.lastInsertId().executeAsOne().also { refresh() }
        }

    override suspend fun update(ingredient: Ingredient): Unit =
        withContext(Dispatchers.IO) {
            queries.updateAll(
                name = ingredient.name,
                purchaseUnit = ingredient.purchaseUnit,
                purchasePrice = ingredient.purchasePrice,
                contentAmount = ingredient.contentAmount,
                usageUnit = ingredient.usageUnit,
                updatedAt = System.currentTimeMillis(),
                id = ingredient.id
            )
            refresh()
            Unit
        }

    override suspend fun updatePrice(id: Long, newPrice: Double): Unit =
        withContext(Dispatchers.IO) {
            queries.updatePrice(
                price = newPrice,
                updatedAt = System.currentTimeMillis(),
                id = id
            )
            refresh()
            Unit
        }

    override suspend fun delete(id: Long): Unit =
        withContext(Dispatchers.IO) {
            queries.softDelete(id)
            refresh()
            Unit
        }
}