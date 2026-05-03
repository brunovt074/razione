package com.recipecostcalculator.infrastructure.persistence.sqlite

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class AndroidSQLiteDatabase(
    context: Context,
) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        applyMigrations(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        applyMigrations(db)
    }

    fun read(): SQLiteDatabase = readableDatabase

    fun write(): SQLiteDatabase = writableDatabase

    private fun applyMigrations(db: SQLiteDatabase) {
        migrationStatements().forEach { statement ->
            db.execSQL(statement)
        }
    }

    private fun migrationStatements(): List<String> {
        return listOf(
            """
            CREATE TABLE IF NOT EXISTS ingredients (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                unit TEXT NOT NULL,
                cost_per_unit TEXT NOT NULL,
                currency TEXT NOT NULL
            )
            """.trimIndent(),
            """
            CREATE TABLE IF NOT EXISTS recipes (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                yield_value TEXT NOT NULL,
                yield_unit TEXT NOT NULL
            )
            """.trimIndent(),
            """
            CREATE TABLE IF NOT EXISTS recipe_components (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                recipe_id INTEGER NOT NULL,
                type TEXT NOT NULL,
                reference_id INTEGER NOT NULL,
                quantity_value TEXT NOT NULL,
                quantity_unit TEXT NOT NULL,
                FOREIGN KEY(recipe_id) REFERENCES recipes(id) ON DELETE CASCADE
            )
            """.trimIndent(),
            """
            CREATE TABLE IF NOT EXISTS fixed_cost_items (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                monthly_amount TEXT NOT NULL,
                currency TEXT NOT NULL
            )
            """.trimIndent(),
            """
            CREATE TABLE IF NOT EXISTS fixed_cost_configuration (
                id INTEGER PRIMARY KEY CHECK (id = 1),
                pizzas_per_month TEXT NOT NULL
            )
            """.trimIndent(),
            "INSERT OR IGNORE INTO fixed_cost_configuration(id, pizzas_per_month) VALUES (1, '0')",
            """
            CREATE TABLE IF NOT EXISTS pricing_configuration (
                id INTEGER PRIMARY KEY CHECK (id = 1),
                selling_price_per_pizza TEXT NOT NULL,
                currency TEXT NOT NULL,
                commission_percent TEXT NOT NULL,
                waste_percent TEXT NOT NULL,
                target_margin_percent TEXT NOT NULL
            )
            """.trimIndent(),
            """
            INSERT OR IGNORE INTO pricing_configuration(
                id,
                selling_price_per_pizza,
                currency,
                commission_percent,
                waste_percent,
                target_margin_percent
            ) VALUES (1, '0', 'ARS', '0', '0', '50')
            """.trimIndent(),
            """
            CREATE TABLE IF NOT EXISTS price_list_configuration (
                id INTEGER PRIMARY KEY CHECK (id = 1),
                cost_basis_type TEXT NOT NULL
            )
            """.trimIndent(),
            "INSERT OR IGNORE INTO price_list_configuration(id, cost_basis_type) VALUES (1, 'VARIABLE')",
            """
            CREATE TABLE IF NOT EXISTS price_list_products (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                selling_price TEXT NOT NULL,
                currency TEXT NOT NULL,
                notes TEXT NOT NULL DEFAULT ''
            )
            """.trimIndent(),
        )
    }

    companion object {
        private const val DATABASE_NAME = "recipe-cost.db"
        private const val DATABASE_VERSION = 2
    }
}
