package com.recipecostcalculator.infrastructure.persistence.sqlite.repository

import android.content.ContentValues
import com.recipecostcalculator.infrastructure.persistence.sqlite.AndroidSQLiteDatabase
import com.recipecostcalculator.recipe.domain.model.IngredientComponent
import com.recipecostcalculator.recipe.domain.model.Quantity
import com.recipecostcalculator.recipe.domain.model.Recipe
import com.recipecostcalculator.recipe.domain.model.RecipeComponent
import com.recipecostcalculator.recipe.domain.model.SubRecipeComponent
import com.recipecostcalculator.recipe.domain.model.UnitOfMeasure
import com.recipecostcalculator.recipe.domain.repository.RecipeRepository

class AndroidSQLiteRecipeRepository(
    private val database: AndroidSQLiteDatabase,
) : RecipeRepository {

    override fun save(recipe: Recipe): Recipe {
        val db = database.write()
        db.beginTransaction()
        return try {
            val recipeId = if (recipe.id == 0L) {
                createRecipe(db, recipe)
            } else {
                updateRecipe(db, recipe)
                recipe.id
            }

            replaceComponents(
                db = db,
                recipeId = recipeId,
                components = recipe.components,
            )

            db.setTransactionSuccessful()
            findById(recipeId) ?: throw IllegalStateException("No se pudo cargar la receta guardada")
        } finally {
            db.endTransaction()
        }
    }

    override fun findById(id: Long): Recipe? {
        val db = database.read()
        val cursor = db.query(
            "recipes",
            arrayOf("id", "name", "yield_value", "yield_unit"),
            "id = ?",
            arrayOf(id.toString()),
            null,
            null,
            null,
        )

        cursor.use {
            if (!it.moveToFirst()) {
                return null
            }

            return mapRecipe(
                cursor = it,
                components = loadComponents(db, id),
            )
        }
    }

    override fun findAll(): List<Recipe> {
        val db = database.read()
        val cursor = db.query(
            "recipes",
            arrayOf("id", "name", "yield_value", "yield_unit"),
            null,
            null,
            null,
            null,
            "name ASC",
        )

        cursor.use {
            val recipes = mutableListOf<Recipe>()
            while (it.moveToNext()) {
                val recipeId = it.getLong(it.getColumnIndexOrThrow("id"))
                recipes += mapRecipe(it, loadComponents(db, recipeId))
            }
            return recipes
        }
    }

    private fun createRecipe(db: android.database.sqlite.SQLiteDatabase, recipe: Recipe): Long {
        val values = ContentValues().apply {
            put("name", recipe.name)
            put("yield_value", recipe.yield.value.toPlainString())
            put("yield_unit", recipe.yield.unit.symbol)
        }
        val generatedId = db.insertOrThrow("recipes", null, values)
        check(generatedId > 0) { "No se pudo recuperar el id generado para receta" }
        return generatedId
    }

    private fun updateRecipe(db: android.database.sqlite.SQLiteDatabase, recipe: Recipe) {
        val recipeId = recipe.id
        require(recipeId > 0L) { "Receta sin id persistido" }
        val values = ContentValues().apply {
            put("name", recipe.name)
            put("yield_value", recipe.yield.value.toPlainString())
            put("yield_unit", recipe.yield.unit.symbol)
        }
        val updatedRows = db.update(
            "recipes",
            values,
            "id = ?",
            arrayOf(recipeId.toString()),
        )
        require(updatedRows > 0) { "Receta no encontrada: $recipeId" }
    }

    private fun replaceComponents(
        db: android.database.sqlite.SQLiteDatabase,
        recipeId: Long,
        components: List<RecipeComponent>,
    ) {
        db.delete("recipe_components", "recipe_id = ?", arrayOf(recipeId.toString()))

        components.forEach { component ->
            val values = ContentValues().apply {
                put("recipe_id", recipeId)
                when (component) {
                    is IngredientComponent -> {
                        put("type", "INGREDIENT")
                        put("reference_id", component.ingredientId)
                    }

                    is SubRecipeComponent -> {
                        put("type", "RECIPE")
                        put("reference_id", component.recipeId)
                    }
                }
                put("quantity_value", component.quantity.value.toPlainString())
                put("quantity_unit", component.quantity.unit.symbol)
            }
            db.insertOrThrow("recipe_components", null, values)
        }
    }

    private fun loadComponents(
        db: android.database.sqlite.SQLiteDatabase,
        recipeId: Long,
    ): List<RecipeComponent> {
        val cursor = db.query(
            "recipe_components",
            arrayOf("type", "reference_id", "quantity_value", "quantity_unit"),
            "recipe_id = ?",
            arrayOf(recipeId.toString()),
            null,
            null,
            null,
        )

        cursor.use {
            val components = mutableListOf<RecipeComponent>()
            while (it.moveToNext()) {
                components += mapComponent(it)
            }
            return components
        }
    }

    private fun mapRecipe(cursor: android.database.Cursor, components: List<RecipeComponent>): Recipe {
        return Recipe(
            id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
            name = cursor.getString(cursor.getColumnIndexOrThrow("name")),
            yield = Quantity.of(
                value = cursor.getString(cursor.getColumnIndexOrThrow("yield_value")),
                unit = UnitOfMeasure.fromRaw(cursor.getString(cursor.getColumnIndexOrThrow("yield_unit"))),
            ),
            components = components,
        )
    }

    private fun mapComponent(cursor: android.database.Cursor): RecipeComponent {
        val quantity = Quantity.of(
            value = cursor.getString(cursor.getColumnIndexOrThrow("quantity_value")),
            unit = UnitOfMeasure.fromRaw(cursor.getString(cursor.getColumnIndexOrThrow("quantity_unit"))),
        )

        return when (cursor.getString(cursor.getColumnIndexOrThrow("type"))) {
            "INGREDIENT" -> IngredientComponent(
                ingredientId = cursor.getLong(cursor.getColumnIndexOrThrow("reference_id")),
                quantity = quantity,
            )

            "RECIPE" -> SubRecipeComponent(
                recipeId = cursor.getLong(cursor.getColumnIndexOrThrow("reference_id")),
                quantity = quantity,
            )

            else -> throw IllegalStateException("Tipo de componente no soportado")
        }
    }
}
