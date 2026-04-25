package com.recipecostcalculator.infrastructure.persistence.sqlite

import java.sql.Connection
import java.sql.DriverManager

class SQLiteDatabase(
    databasePath: String = "recipe-cost.db",
) {
    private val jdbcUrl = "jdbc:sqlite:$databasePath"

    init {
        Class.forName("org.sqlite.JDBC")
        initializeSchema()
    }

    fun <T> withConnection(block: (Connection) -> T): T {
        DriverManager.getConnection(jdbcUrl).use { connection ->
            connection.createStatement().use { statement ->
                statement.execute("PRAGMA foreign_keys = ON")
            }
            return block(connection)
        }
    }

    private fun initializeSchema() {
        withConnection { connection ->
            connection.createStatement().use { statement ->
                statement.executeUpdate(
                    """
                    CREATE TABLE IF NOT EXISTS ingredients (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        name TEXT NOT NULL,
                        unit TEXT NOT NULL,
                        cost_per_unit TEXT NOT NULL,
                        currency TEXT NOT NULL
                    )
                    """.trimIndent(),
                )

                statement.executeUpdate(
                    """
                    CREATE TABLE IF NOT EXISTS recipes (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        name TEXT NOT NULL,
                        yield_value TEXT NOT NULL,
                        yield_unit TEXT NOT NULL
                    )
                    """.trimIndent(),
                )

                statement.executeUpdate(
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
                )
            }
        }
    }
}
