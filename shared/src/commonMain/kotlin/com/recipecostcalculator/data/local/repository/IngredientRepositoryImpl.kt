package com.recipecostcalculator.data.local.repository

import com.recipecostcalculator.db.PizzeriaDatabase
import com.recipecostcalculator.domain.model.Ingredient
import com.recipecostcalculator.domain.repository.IngredientRepository
import com.recipecostcalculator.financial.domain.model.Money
import com.recipecostcalculator.financial.domain.model.Quantity
import com.recipecostcalculator.measurement.MeasurementDimension
import com.recipecostcalculator.measurement.MeasurementUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
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

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeAll(): Flow<List<Ingredient>> = refreshSignal.flatMapLatest {
        kotlinx.coroutines.flow.flow {
            val list = queries.selectAll().executeAsList().map { row -> row.toIngredient() }
            emit(list)
        }
    }

    private suspend fun refresh() {
        refreshSignal.emit(Unit)
    }

    override suspend fun getById(id: Long): Ingredient? =
        withContext(Dispatchers.IO) {
            queries.selectById(id).executeAsOneOrNull()?.toIngredient()
        }

    override suspend fun getAllActive(): List<Ingredient> =
        withContext(Dispatchers.IO) {
            queries.selectAll().executeAsList().map { row -> row.toIngredient() }
        }

    override suspend fun insert(ingredient: Ingredient): Long =
        withContext(Dispatchers.IO) {
            queries.insert(
                name = ingredient.name,
                dimension = ingredient.dimension.name,
                purchaseUnit = ingredient.purchaseUnit.name,
                purchasePackageLabel = ingredient.purchasePackageLabel,
                purchasePrice = ingredient.purchasePrice.amount,
                contentAmount = ingredient.contentAmount.value,
                usageUnit = ingredient.usageUnit.name,
                updatedAt = ingredient.updatedAt
            )
            queries.lastInsertId().executeAsOne().also { refresh() }
        }

    override suspend fun update(ingredient: Ingredient): Unit =
        withContext(Dispatchers.IO) {
            queries.updateAll(
                name = ingredient.name,
                dimension = ingredient.dimension.name,
                purchaseUnit = ingredient.purchaseUnit.name,
                purchasePackageLabel = ingredient.purchasePackageLabel,
                purchasePrice = ingredient.purchasePrice.amount,
                contentAmount = ingredient.contentAmount.value,
                usageUnit = ingredient.usageUnit.name,
                updatedAt = System.currentTimeMillis(),
                id = ingredient.id
            )
            refresh()
            Unit
        }

    override suspend fun updatePrice(id: Long, newPrice: Money, updatedAt: Long): Unit =
        withContext(Dispatchers.IO) {
            queries.updatePrice(
                price = newPrice.amount,
                updatedAt = updatedAt,
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

private fun com.recipecostcalculator.db.Ingredients.toIngredient(): Ingredient {
    val dimension = parseDimension(dimension)
    val canonical = MeasurementUnit.canonicalFor(dimension)
    return Ingredient(
        id = id,
        name = name,
        dimension = dimension,
        purchaseUnit = parseUnit(purchase_unit, dimension),
        purchasePackageLabel = purchase_package_label,
        purchasePrice = Money(purchase_price),
        contentAmount = Quantity(content_amount, canonical),
        usageUnit = parseUnit(usage_unit, dimension),
        isActive = is_active == 1L,
        updatedAt = updated_at,
    )
}

private fun parseDimension(raw: String): MeasurementDimension =
    MeasurementDimension.values().firstOrNull { it.name == raw } ?: MeasurementDimension.MASS

private fun parseUnit(raw: String, dimension: MeasurementDimension): MeasurementUnit =
    MeasurementUnit.values().firstOrNull { it.name == raw && it.dimension == dimension }
        ?: MeasurementUnit.canonicalFor(dimension)
