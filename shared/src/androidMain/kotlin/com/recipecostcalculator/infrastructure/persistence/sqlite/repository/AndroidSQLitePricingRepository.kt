package com.recipecostcalculator.infrastructure.persistence.sqlite.repository

import android.content.ContentValues
import com.recipecostcalculator.financial.domain.model.Money
import com.recipecostcalculator.infrastructure.persistence.sqlite.AndroidSQLiteDatabase
import com.recipecostcalculator.pricing.domain.model.CostBasisType
import com.recipecostcalculator.pricing.domain.model.PriceListConfiguration
import com.recipecostcalculator.pricing.domain.model.PriceListProduct
import com.recipecostcalculator.pricing.domain.model.PricingConfiguration
import com.recipecostcalculator.pricing.domain.repository.PricingRepository
import java.math.BigDecimal

class AndroidSQLitePricingRepository(
    private val database: AndroidSQLiteDatabase,
) : PricingRepository {

    override fun savePricingConfiguration(configuration: PricingConfiguration): PricingConfiguration {
        val db = database.write()
        val values = ContentValues().apply {
            put("id", 1)
            put("selling_price_per_pizza", configuration.sellingPricePerPizza.amount.toPlainString())
            put("currency", configuration.sellingPricePerPizza.currency)
            put("commission_percent", configuration.commissionPercent.toPlainString())
            put("waste_percent", configuration.wastePercent.toPlainString())
            put("target_margin_percent", configuration.targetMarginPercent.toPlainString())
        }
        db.insertWithOnConflict(
            "pricing_configuration",
            null,
            values,
            android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE,
        )
        return configuration
    }

    override fun getPricingConfiguration(): PricingConfiguration {
        val db = database.read()
        val cursor = db.query(
            "pricing_configuration",
            arrayOf("selling_price_per_pizza", "currency", "commission_percent", "waste_percent", "target_margin_percent"),
            "id = ?",
            arrayOf("1"),
            null,
            null,
            null,
        )

        cursor.use {
            if (it.moveToFirst()) {
                return mapPricingConfiguration(it)
            }
        }

        val defaults = PricingConfiguration.defaults()
        savePricingConfiguration(defaults)
        return defaults
    }

    override fun savePriceListConfiguration(configuration: PriceListConfiguration): PriceListConfiguration {
        val db = database.write()
        val values = ContentValues().apply {
            put("id", 1)
            put("cost_basis_type", configuration.costBasisType.name)
        }
        db.insertWithOnConflict(
            "price_list_configuration",
            null,
            values,
            android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE,
        )
        return configuration
    }

    override fun getPriceListConfiguration(): PriceListConfiguration {
        val db = database.read()
        val cursor = db.query(
            "price_list_configuration",
            arrayOf("cost_basis_type"),
            "id = ?",
            arrayOf("1"),
            null,
            null,
            null,
        )

        cursor.use {
            if (it.moveToFirst()) {
                return PriceListConfiguration(
                    costBasisType = CostBasisType.valueOf(it.getString(it.getColumnIndexOrThrow("cost_basis_type"))),
                )
            }
        }

        val defaults = PriceListConfiguration.defaults()
        savePriceListConfiguration(defaults)
        return defaults
    }

    override fun saveProduct(product: PriceListProduct): PriceListProduct {
        return if (product.id == 0L) {
            createProduct(product)
        } else {
            updateProduct(product)
        }
    }

    override fun findAllProducts(): List<PriceListProduct> {
        val db = database.read()
        val cursor = db.query(
            "price_list_products",
            arrayOf("id", "name", "selling_price", "currency", "notes"),
            null,
            null,
            null,
            null,
            "name ASC",
        )

        cursor.use {
            val products = mutableListOf<PriceListProduct>()
            while (it.moveToNext()) {
                products += mapProduct(it)
            }
            return products
        }
    }

    private fun createProduct(product: PriceListProduct): PriceListProduct {
        val db = database.write()
        val values = ContentValues().apply {
            put("name", product.name)
            put("selling_price", product.sellingPrice.amount.toPlainString())
            put("currency", product.sellingPrice.currency)
            put("notes", product.notes)
        }
        val generatedId = db.insertOrThrow("price_list_products", null, values)
        check(generatedId > 0) { "No se pudo recuperar el id generado para producto" }
        return product.copy(id = generatedId)
    }

    private fun updateProduct(product: PriceListProduct): PriceListProduct {
        val productId = product.id
        require(productId > 0L) { "Producto sin id persistido" }
        val db = database.write()
        val values = ContentValues().apply {
            put("name", product.name)
            put("selling_price", product.sellingPrice.amount.toPlainString())
            put("currency", product.sellingPrice.currency)
            put("notes", product.notes)
        }
        val updatedRows = db.update(
            "price_list_products",
            values,
            "id = ?",
            arrayOf(productId.toString()),
        )
        require(updatedRows > 0) { "Producto no encontrado: $productId" }
        return product
    }

    private fun mapPricingConfiguration(cursor: android.database.Cursor): PricingConfiguration {
        return PricingConfiguration(
            sellingPricePerPizza = Money.of(
                value = cursor.getString(cursor.getColumnIndexOrThrow("selling_price_per_pizza")),
                currency = cursor.getString(cursor.getColumnIndexOrThrow("currency")),
            ),
            commissionPercent = BigDecimal(cursor.getString(cursor.getColumnIndexOrThrow("commission_percent"))),
            wastePercent = BigDecimal(cursor.getString(cursor.getColumnIndexOrThrow("waste_percent"))),
            targetMarginPercent = BigDecimal(cursor.getString(cursor.getColumnIndexOrThrow("target_margin_percent"))),
        )
    }

    private fun mapProduct(cursor: android.database.Cursor): PriceListProduct {
        return PriceListProduct(
            id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
            name = cursor.getString(cursor.getColumnIndexOrThrow("name")),
            sellingPrice = Money.of(
                value = cursor.getString(cursor.getColumnIndexOrThrow("selling_price")),
                currency = cursor.getString(cursor.getColumnIndexOrThrow("currency")),
            ),
            notes = cursor.getString(cursor.getColumnIndexOrThrow("notes")) ?: "",
        )
    }
}
