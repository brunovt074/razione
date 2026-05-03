package com.recipecostcalculator.infrastructure.persistence.sqlite.repository

import android.content.ContentValues
import com.recipecostcalculator.financial.domain.model.Money
import com.recipecostcalculator.fixedcost.domain.model.FixedCostItem
import com.recipecostcalculator.fixedcost.domain.model.ProductionConfiguration
import com.recipecostcalculator.fixedcost.domain.repository.FixedCostRepository
import com.recipecostcalculator.infrastructure.persistence.sqlite.AndroidSQLiteDatabase
import java.math.BigDecimal

class AndroidSQLiteFixedCostRepository(
    private val database: AndroidSQLiteDatabase,
) : FixedCostRepository {

    override fun saveItem(item: FixedCostItem): FixedCostItem {
        return if (item.id == 0L) {
            create(item)
        } else {
            update(item)
        }
    }

    override fun findAllItems(): List<FixedCostItem> {
        val db = database.read()
        val cursor = db.query(
            "fixed_cost_items",
            arrayOf("id", "name", "monthly_amount", "currency"),
            null,
            null,
            null,
            null,
            "name ASC",
        )

        cursor.use {
            val items = mutableListOf<FixedCostItem>()
            while (it.moveToNext()) {
                items += mapFixedCostItem(it)
            }
            return items
        }
    }

    override fun saveProductionConfiguration(configuration: ProductionConfiguration): ProductionConfiguration {
        val db = database.write()
        val values = ContentValues().apply {
            put("id", 1)
            put("pizzas_per_month", configuration.pizzasPerMonth.toPlainString())
        }
        db.insertWithOnConflict(
            "fixed_cost_configuration",
            null,
            values,
            android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE,
        )
        return configuration
    }

    override fun getProductionConfiguration(): ProductionConfiguration {
        val db = database.read()
        val cursor = db.query(
            "fixed_cost_configuration",
            arrayOf("pizzas_per_month"),
            "id = ?",
            arrayOf("1"),
            null,
            null,
            null,
        )

        cursor.use {
            return if (it.moveToFirst()) {
                ProductionConfiguration(
                    pizzasPerMonth = BigDecimal(it.getString(it.getColumnIndexOrThrow("pizzas_per_month"))),
                )
            } else {
                ProductionConfiguration(pizzasPerMonth = BigDecimal.ZERO)
            }
        }
    }

    private fun create(item: FixedCostItem): FixedCostItem {
        val db = database.write()
        val values = ContentValues().apply {
            put("name", item.name)
            put("monthly_amount", item.monthlyAmount.amount.toPlainString())
            put("currency", item.monthlyAmount.currency)
        }
        val generatedId = db.insertOrThrow("fixed_cost_items", null, values)
        check(generatedId > 0) { "No se pudo recuperar el id generado para gasto fijo" }
        return item.copy(id = generatedId)
    }

    private fun update(item: FixedCostItem): FixedCostItem {
        val itemId = item.id
        require(itemId > 0) { "Gasto fijo sin id persistido" }
        val db = database.write()
        val values = ContentValues().apply {
            put("name", item.name)
            put("monthly_amount", item.monthlyAmount.amount.toPlainString())
            put("currency", item.monthlyAmount.currency)
        }
        val updatedRows = db.update(
            "fixed_cost_items",
            values,
            "id = ?",
            arrayOf(itemId.toString()),
        )
        require(updatedRows > 0) { "Gasto fijo no encontrado: $itemId" }
        return item
    }

    private fun mapFixedCostItem(cursor: android.database.Cursor): FixedCostItem {
        return FixedCostItem(
            id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
            name = cursor.getString(cursor.getColumnIndexOrThrow("name")),
            monthlyAmount = Money.of(
                value = cursor.getString(cursor.getColumnIndexOrThrow("monthly_amount")),
                currency = cursor.getString(cursor.getColumnIndexOrThrow("currency")),
            ),
        )
    }
}
