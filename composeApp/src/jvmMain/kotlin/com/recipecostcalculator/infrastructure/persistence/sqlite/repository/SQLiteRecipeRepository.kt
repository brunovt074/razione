package com.recipecostcalculator.infrastructure.persistence.sqlite.repository

import com.recipecostcalculator.domain.model.recipe.IngredientComponent
import com.recipecostcalculator.domain.model.recipe.Recipe
import com.recipecostcalculator.domain.model.recipe.RecipeComponent
import com.recipecostcalculator.domain.model.recipe.SubRecipeComponent
import com.recipecostcalculator.domain.model.valueobject.Quantity
import com.recipecostcalculator.domain.model.valueobject.UnitOfMeasure
import com.recipecostcalculator.domain.repository.RecipeRepository
import com.recipecostcalculator.infrastructure.persistence.sqlite.SQLiteDatabase
import java.sql.Connection
import java.sql.ResultSet
import java.sql.Statement

class SQLiteRecipeRepository(
    private val database: SQLiteDatabase,
) : RecipeRepository {

    override fun save(recipe: Recipe): Recipe {
        val recipeId: Long = database.withConnection { connection ->
            connection.autoCommit = false
            try {
                val persistedId = if (recipe.id == null) {
                    createRecipe(connection, recipe)
                } else {
                    updateRecipe(connection, recipe)
                    requireNotNull(recipe.id)
                }

                replaceComponents(
                    connection = connection,
                    recipeId = persistedId,
                    components = recipe.components,
                )

                connection.commit()
                persistedId
            } catch (exception: Exception) {
                connection.rollback()
                throw exception
            } finally {
                connection.autoCommit = true
            }
        }

        return findById(recipeId) ?: throw IllegalStateException("No se pudo cargar la receta guardada")
    }

    override fun findById(id: Long): Recipe? {
        return database.withConnection { connection ->
            connection.prepareStatement(
                "SELECT id, name, yield_value, yield_unit FROM recipes WHERE id = ?",
            ).use { statement ->
                statement.setLong(1, id)
                statement.executeQuery().use { resultSet ->
                    if (!resultSet.next()) {
                        null
                    } else {
                        mapRecipe(resultSet, loadComponents(connection, id))
                    }
                }
            }
        }
    }

    override fun findAll(): List<Recipe> {
        return database.withConnection { connection ->
            connection.prepareStatement(
                "SELECT id, name, yield_value, yield_unit FROM recipes ORDER BY name ASC",
            ).use { statement ->
                statement.executeQuery().use { resultSet ->
                    val recipes = mutableListOf<Recipe>()
                    while (resultSet.next()) {
                        val recipeId = resultSet.getLong("id")
                        recipes += mapRecipe(resultSet, loadComponents(connection, recipeId))
                    }
                    recipes
                }
            }
        }
    }

    private fun createRecipe(connection: Connection, recipe: Recipe): Long {
        return connection.prepareStatement(
            "INSERT INTO recipes(name, yield_value, yield_unit) VALUES (?, ?, ?)",
            Statement.RETURN_GENERATED_KEYS,
        ).use { statement ->
            statement.setString(1, recipe.name)
            statement.setString(2, recipe.yield.value.toPlainString())
            statement.setString(3, recipe.yield.unit.symbol)
            statement.executeUpdate()

            statement.generatedKeys.use { keys ->
                check(keys.next()) { "No se pudo recuperar el id generado para receta" }
                keys.getLong(1)
            }
        }
    }

    private fun updateRecipe(connection: Connection, recipe: Recipe) {
        val recipeId = requireNotNull(recipe.id)
        connection.prepareStatement(
            "UPDATE recipes SET name = ?, yield_value = ?, yield_unit = ? WHERE id = ?",
        ).use { statement ->
            statement.setString(1, recipe.name)
            statement.setString(2, recipe.yield.value.toPlainString())
            statement.setString(3, recipe.yield.unit.symbol)
            statement.setLong(4, recipeId)
            val updatedRows = statement.executeUpdate()
            require(updatedRows > 0) { "Receta no encontrada: $recipeId" }
        }
    }

    private fun replaceComponents(
        connection: Connection,
        recipeId: Long,
        components: List<RecipeComponent>,
    ) {
        connection.prepareStatement("DELETE FROM recipe_components WHERE recipe_id = ?").use { statement ->
            statement.setLong(1, recipeId)
            statement.executeUpdate()
        }

        if (components.isEmpty()) {
            return
        }

        connection.prepareStatement(
            """
            INSERT INTO recipe_components(recipe_id, type, reference_id, quantity_value, quantity_unit)
            VALUES (?, ?, ?, ?, ?)
            """.trimIndent(),
        ).use { statement ->
            components.forEach { component ->
                statement.setLong(1, recipeId)
                when (component) {
                    is IngredientComponent -> {
                        statement.setString(2, "INGREDIENT")
                        statement.setLong(3, component.ingredientId)
                    }

                    is SubRecipeComponent -> {
                        statement.setString(2, "RECIPE")
                        statement.setLong(3, component.recipeId)
                    }
                }
                statement.setString(4, component.quantity.value.toPlainString())
                statement.setString(5, component.quantity.unit.symbol)
                statement.addBatch()
            }
            statement.executeBatch()
        }
    }

    private fun loadComponents(connection: Connection, recipeId: Long): List<RecipeComponent> {
        return connection.prepareStatement(
            "SELECT type, reference_id, quantity_value, quantity_unit FROM recipe_components WHERE recipe_id = ?",
        ).use { statement ->
            statement.setLong(1, recipeId)
            statement.executeQuery().use { resultSet ->
                val components = mutableListOf<RecipeComponent>()
                while (resultSet.next()) {
                    components += mapComponent(resultSet)
                }
                components
            }
        }
    }

    private fun mapRecipe(resultSet: ResultSet, components: List<RecipeComponent>): Recipe {
        return Recipe(
            id = resultSet.getLong("id"),
            name = resultSet.getString("name"),
            yield = Quantity.of(
                value = resultSet.getString("yield_value"),
                unit = UnitOfMeasure.fromRaw(resultSet.getString("yield_unit")),
            ),
            components = components,
        )
    }

    private fun mapComponent(resultSet: ResultSet): RecipeComponent {
        val quantity = Quantity.of(
            value = resultSet.getString("quantity_value"),
            unit = UnitOfMeasure.fromRaw(resultSet.getString("quantity_unit")),
        )

        return when (resultSet.getString("type")) {
            "INGREDIENT" -> IngredientComponent(
                ingredientId = resultSet.getLong("reference_id"),
                quantity = quantity,
            )

            "RECIPE" -> SubRecipeComponent(
                recipeId = resultSet.getLong("reference_id"),
                quantity = quantity,
            )

            else -> throw IllegalStateException("Tipo de componente no soportado")
        }
    }
}
