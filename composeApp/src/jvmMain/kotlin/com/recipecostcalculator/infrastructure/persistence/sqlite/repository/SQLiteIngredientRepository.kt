package com.recipecostcalculator.infrastructure.persistence.sqlite.repository

import com.recipecostcalculator.domain.model.ingredient.Ingredient
import com.recipecostcalculator.domain.model.valueobject.Money
import com.recipecostcalculator.domain.model.valueobject.UnitOfMeasure
import com.recipecostcalculator.domain.repository.IngredientRepository
import com.recipecostcalculator.infrastructure.persistence.sqlite.SQLiteDatabase
import java.sql.ResultSet
import java.sql.Statement

class SQLiteIngredientRepository(
    private val database: SQLiteDatabase,
) : IngredientRepository {

    override fun save(ingredient: Ingredient): Ingredient {
        return if (ingredient.id == null) {
            create(ingredient)
        } else {
            update(ingredient)
        }
    }

    override fun findById(id: Long): Ingredient? {
        return database.withConnection { connection ->
            connection.prepareStatement(
                "SELECT id, name, unit, cost_per_unit, currency FROM ingredients WHERE id = ?",
            ).use { statement ->
                statement.setLong(1, id)
                statement.executeQuery().use { resultSet ->
                    if (!resultSet.next()) {
                        null
                    } else {
                        mapIngredient(resultSet)
                    }
                }
            }
        }
    }

    override fun findAll(): List<Ingredient> {
        return database.withConnection { connection ->
            connection.prepareStatement(
                "SELECT id, name, unit, cost_per_unit, currency FROM ingredients ORDER BY name ASC",
            ).use { statement ->
                statement.executeQuery().use { resultSet ->
                    val ingredients = mutableListOf<Ingredient>()
                    while (resultSet.next()) {
                        ingredients += mapIngredient(resultSet)
                    }
                    ingredients
                }
            }
        }
    }

    private fun create(ingredient: Ingredient): Ingredient {
        return database.withConnection { connection ->
            connection.prepareStatement(
                "INSERT INTO ingredients(name, unit, cost_per_unit, currency) VALUES (?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS,
            ).use { statement ->
                statement.setString(1, ingredient.name)
                statement.setString(2, ingredient.unit.symbol)
                statement.setString(3, ingredient.costPerUnit.amount.toPlainString())
                statement.setString(4, ingredient.costPerUnit.currency)
                statement.executeUpdate()

                statement.generatedKeys.use { keys ->
                    check(keys.next()) { "No se pudo recuperar el id generado para ingrediente" }
                    ingredient.copy(id = keys.getLong(1))
                }
            }
        }
    }

    private fun update(ingredient: Ingredient): Ingredient {
        val ingredientId = requireNotNull(ingredient.id)
        return database.withConnection { connection ->
            connection.prepareStatement(
                "UPDATE ingredients SET name = ?, unit = ?, cost_per_unit = ?, currency = ? WHERE id = ?",
            ).use { statement ->
                statement.setString(1, ingredient.name)
                statement.setString(2, ingredient.unit.symbol)
                statement.setString(3, ingredient.costPerUnit.amount.toPlainString())
                statement.setString(4, ingredient.costPerUnit.currency)
                statement.setLong(5, ingredientId)
                val updatedRows = statement.executeUpdate()
                require(updatedRows > 0) { "Ingrediente no encontrado: $ingredientId" }
                ingredient
            }
        }
    }

    private fun mapIngredient(resultSet: ResultSet): Ingredient {
        return Ingredient(
            id = resultSet.getLong("id"),
            name = resultSet.getString("name"),
            unit = UnitOfMeasure.fromRaw(resultSet.getString("unit")),
            costPerUnit = Money.of(
                value = resultSet.getString("cost_per_unit"),
                currency = resultSet.getString("currency"),
            ),
        )
    }
}
