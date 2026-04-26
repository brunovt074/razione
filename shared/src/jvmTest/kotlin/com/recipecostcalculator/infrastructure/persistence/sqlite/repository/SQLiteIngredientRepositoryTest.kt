package com.recipecostcalculator.infrastructure.persistence.sqlite.repository

import com.recipecostcalculator.financial.domain.model.Money
import com.recipecostcalculator.infrastructure.persistence.sqlite.SQLiteDatabase
import com.recipecostcalculator.recipe.domain.model.UnitOfMeasure
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import com.recipecostcalculator.ingredient.domain.model.Ingredient

class SQLiteIngredientRepositoryTest {
    private lateinit var dbFile: File
    private lateinit var repository: SQLiteIngredientRepository

    @BeforeTest
    fun setUp() {
        dbFile = File.createTempFile("ingredient_repo_", ".db")
        dbFile.delete()
        repository = SQLiteIngredientRepository(SQLiteDatabase(dbFile.absolutePath))
    }

    @AfterTest
    fun tearDown() {
        if (dbFile.exists()) {
            dbFile.delete()
        }
    }

    @Test
    fun `should save ingredient with generated id`() {
        val ingredient = Ingredient(
            name = "Harina",
            unit = UnitOfMeasure.GRAM,
            costPerUnit = Money.of("0.01"),
        )

        val saved = repository.save(ingredient)

        assertTrue(saved.id > 0)
        assertEquals("Harina", saved.name)
    }

    @Test
    fun `should find ingredient by id`() {
        val saved = repository.save(
            Ingredient(
                name = "Azucar",
                unit = UnitOfMeasure.GRAM,
                costPerUnit = Money.of("0.02"),
            ),
        )

        val found = repository.findById(saved.id)

        assertNotNull(found)
        assertEquals(saved.id, found.id)
        assertEquals("Azucar", found.name)
    }

    @Test
    fun `should update existing ingredient`() {
        val saved = repository.save(
            Ingredient(
                name = "Leche",
                unit = UnitOfMeasure.LITER,
                costPerUnit = Money.of("1.50"),
            ),
        )

        val updated = repository.save(saved.copy(costPerUnit = Money.of("2.00")))
        val found = repository.findById(saved.id)

        assertEquals(saved.id, updated.id)
        assertNotNull(found)
        assertEquals("2", found.costPerUnit.amount.toPlainString())
    }

    @Test
    fun `should return null for missing ingredient`() {
        val result = repository.findById(99999L)

        assertNull(result)
    }

    @Test
    fun `should return ingredients ordered by name`() {
        repository.save(
            Ingredient(
                name = "Sal",
                unit = UnitOfMeasure.GRAM,
                costPerUnit = Money.of("0.03"),
            ),
        )
        repository.save(
            Ingredient(
                name = "Aceite",
                unit = UnitOfMeasure.MILLILITER,
                costPerUnit = Money.of("0.05"),
            ),
        )

        val all = repository.findAll()

        assertEquals(2, all.size)
        assertEquals("Aceite", all.first().name)
        assertEquals("Sal", all.last().name)
    }
}
