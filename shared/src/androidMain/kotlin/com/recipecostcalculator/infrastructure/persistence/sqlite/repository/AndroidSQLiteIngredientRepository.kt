package com.recipecostcalculator.infrastructure.persistence.sqlite.repository

import android.content.ContentValues
import com.recipecostcalculator.financial.domain.model.Money
import com.recipecostcalculator.ingredient.domain.model.Ingredient
import com.recipecostcalculator.ingredient.domain.repository.IngredientRepository
import com.recipecostcalculator.infrastructure.persistence.sqlite.AndroidSQLiteDatabase
import com.recipecostcalculator.recipe.domain.model.UnitOfMeasure

class AndroidSQLiteIngredientRepository(
    private val database: AndroidSQLiteDatabase,
) : IngredientRepository {

    override fun save(ingredient: Ingredient): Ingredient {
        return if (ingredient.id == 0L) {
            create(ingredient)
        } else {
            update(ingredient)
        }
    }

    override fun findById(id: Long): Ingredient? {
        val db = database.read()
        val cursor = db.query(
            "ingredients",
            arrayOf("id", "name", "unit", "cost_per_unit", "currency"),
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
            return mapIngredient(it)
        }
    }

    override fun findAll(): List<Ingredient> {
        val db = database.read()
        val cursor = db.query(
            "ingredients",
            arrayOf("id", "name", "unit", "cost_per_unit", "currency"),
            null,
            null,
            null,
            null,
            "name ASC",
        )

        cursor.use {
            val items = mutableListOf<Ingredient>()
            while (it.moveToNext()) {
                items += mapIngredient(it)
            }
            return items
        }
    }

    private fun create(ingredient: Ingredient): Ingredient {
        val db = database.write()
        val values = ContentValues().apply {
            put("name", ingredient.name)
            put("unit", ingredient.unit.symbol)
            put("cost_per_unit", ingredient.costPerUnit.amount.toPlainString())
            put("currency", ingredient.costPerUnit.currency)
        }
        val generatedId = db.insertOrThrow("ingredients", null, values)
        check(generatedId > 0) { "No se pudo recuperar el id generado para ingrediente" }
        return ingredient.copy(id = generatedId)
    }

    private fun update(ingredient: Ingredient): Ingredient {
        val ingredientId = ingredient.id
        require(ingredientId > 0L) { "Ingrediente sin id persistido" }

        val db = database.write()
        val values = ContentValues().apply {
            put("name", ingredient.name)
            put("unit", ingredient.unit.symbol)
            put("cost_per_unit", ingredient.costPerUnit.amount.toPlainString())
            put("currency", ingredient.costPerUnit.currency)
        }
        val updatedRows = db.update(
            "ingredients",
            values,
            "id = ?",
            arrayOf(ingredientId.toString()),
        )
        require(updatedRows > 0) { "Ingrediente no encontrado: $ingredientId" }
        return ingredient
    }

    private fun mapIngredient(cursor: android.database.Cursor): Ingredient {
        val id = cursor.getLong(cursor.getColumnIndexOrThrow("id"))
        val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
        val unit = cursor.getString(cursor.getColumnIndexOrThrow("unit"))
        val costPerUnit = cursor.getString(cursor.getColumnIndexOrThrow("cost_per_unit"))
        val currency = cursor.getString(cursor.getColumnIndexOrThrow("currency"))

        return Ingredient(
            id = id,
            name = name,
            unit = UnitOfMeasure.fromRaw(unit),
            costPerUnit = Money.of(value = costPerUnit, currency = currency),
        )
    }
}
