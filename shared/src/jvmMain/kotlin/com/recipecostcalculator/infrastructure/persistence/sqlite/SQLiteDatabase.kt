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
        val migration = loadMigration("db/migration/V1__create_recipe_cost_schema.sql")
        withConnection { connection ->
            connection.createStatement().use { statement ->
                splitStatements(migration).forEach(statement::executeUpdate)
            }
        }
    }

    private fun loadMigration(path: String): String {
        val stream = javaClass.classLoader.getResourceAsStream(path)
            ?: throw IllegalStateException("No se encontro migracion: $path")
        return stream.bufferedReader().use { it.readText() }
    }

    private fun splitStatements(sql: String): List<String> {
        return sql
            .split(";")
            .map(String::trim)
            .filter(String::isNotEmpty)
    }
}
