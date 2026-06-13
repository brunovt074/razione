package com.recipecostcalculator.data.local.repository

import com.recipecostcalculator.db.PizzeriaDatabase
import com.recipecostcalculator.domain.model.Category
import com.recipecostcalculator.domain.repository.CategoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

class CategoryRepositoryImpl(
    private val db: PizzeriaDatabase
) : CategoryRepository {

    private val queries = db.categoriesQueries
    private val refreshSignal = MutableSharedFlow<Unit>(replay = 1)

    init {
        refreshSignal.tryEmit(Unit)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeAll(): Flow<List<Category>> = refreshSignal.flatMapLatest {
        flow {
            emit(queries.selectAll().executeAsList().map { row ->
                Category(
                    id = row.id,
                    name = row.name,
                    unitLabel = row.unit_label,
                    sortOrder = row.sort_order.toInt(),
                    createdAt = row.created_at,
                    updatedAt = row.updated_at
                )
            })
        }
    }

    override suspend fun getAll(): List<Category> =
        withContext(Dispatchers.IO) {
            queries.selectAll().executeAsList().map { row ->
                Category(
                    id = row.id,
                    name = row.name,
                    unitLabel = row.unit_label,
                    sortOrder = row.sort_order.toInt(),
                    createdAt = row.created_at,
                    updatedAt = row.updated_at
                )
            }
        }

    override suspend fun getAllWithCount(): List<Category> =
        withContext(Dispatchers.IO) {
            queries.selectAllWithCount().executeAsList().map { row ->
                Category(
                    id = row.id,
                    name = row.name,
                    unitLabel = row.unit_label,
                    sortOrder = row.sort_order.toInt(),
                    recipeCount = row.recipe_count.toInt(),
                    createdAt = row.created_at,
                    updatedAt = row.updated_at
                )
            }
        }

    override suspend fun getById(id: Long): Category? =
        withContext(Dispatchers.IO) {
            queries.selectById(id).executeAsOneOrNull()?.let { row ->
                Category(
                    id = row.id,
                    name = row.name,
                    unitLabel = row.unit_label,
                    sortOrder = row.sort_order.toInt(),
                    createdAt = row.created_at,
                    updatedAt = row.updated_at
                )
            }
        }

    override suspend fun insert(category: Category): Long =
        withContext(Dispatchers.IO) {
            db.transactionWithResult {
                queries.insert(
                    name = category.name,
                    unitLabel = category.unitLabel,
                    sortOrder = category.sortOrder.toLong(),
                    createdAt = category.createdAt,
                    updatedAt = category.updatedAt
                )
                queries.lastInsertId().executeAsOne()
            }.also { refresh() }
        }

    override suspend fun update(category: Category): Unit =
        withContext(Dispatchers.IO) {
            queries.update(
                name = category.name,
                unitLabel = category.unitLabel,
                sortOrder = category.sortOrder.toLong(),
                updatedAt = System.currentTimeMillis(),
                id = category.id
            )
            refresh()
        }

    override suspend fun delete(id: Long): Unit =
        withContext(Dispatchers.IO) {
            queries.delete(id)
            refresh()
        }

    override suspend fun countRecipes(categoryId: Long): Long =
        withContext(Dispatchers.IO) {
            queries.countRecipes(categoryId).executeAsOne()
        }

    private suspend fun refresh() {
        refreshSignal.emit(Unit)
    }
}
