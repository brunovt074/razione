package com.recipecostcalculator.infrastructure.persistence.sqlite.repository

import com.recipecostcalculator.financial.domain.model.Money
import com.recipecostcalculator.infrastructure.persistence.sqlite.SQLiteDatabase
import com.recipecostcalculator.ingredient.domain.model.Ingredient
import com.recipecostcalculator.recipe.domain.model.IngredientComponent
import com.recipecostcalculator.recipe.domain.model.Quantity
import com.recipecostcalculator.recipe.domain.model.Recipe
import com.recipecostcalculator.recipe.domain.model.SubRecipeComponent
import com.recipecostcalculator.recipe.domain.model.UnitOfMeasure
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SQLiteRecipeRepositoryTest {
    private lateinit var dbFile: File
    private lateinit var ingredientRepository: SQLiteIngredientRepository
    private lateinit var recipeRepository: SQLiteRecipeRepository

    @BeforeTest
    fun setUp() {
        dbFile = File.createTempFile("recipe_repo_", ".db")
        dbFile.delete()
        val database = SQLiteDatabase(dbFile.absolutePath)
        ingredientRepository = SQLiteIngredientRepository(database)
        recipeRepository = SQLiteRecipeRepository(database)
    }

    @AfterTest
    fun tearDown() {
        if (dbFile.exists()) {
            dbFile.delete()
        }
    }

    @Test
    fun `should save recipe with generated id`() {
        val recipe = Recipe(
            name = "Masa",
            yield = Quantity.of("1", UnitOfMeasure.UNIT),
        )

        val saved = recipeRepository.save(recipe)

        assertTrue(saved.id > 0)
        assertEquals("Masa", saved.name)
    }

    @Test
    fun `should save recipe with ingredient component`() {
        val ingredient = ingredientRepository.save(
            Ingredient(
                name = "Harina",
                unit = UnitOfMeasure.GRAM,
                costPerUnit = Money.of("0.01"),
            ),
        )

        val saved = recipeRepository.save(
            Recipe(
                name = "Pan",
                yield = Quantity.of("2", UnitOfMeasure.UNIT),
                components = listOf(
                    IngredientComponent(
                        ingredientId = ingredient.id,
                        quantity = Quantity.of("500", UnitOfMeasure.GRAM),
                    ),
                ),
            ),
        )

        val found = recipeRepository.findById(saved.id)

        assertNotNull(found)
        assertEquals(1, found.components.size)
    }

    @Test
    fun `should save recipe with sub recipe component`() {
        val base = recipeRepository.save(
            Recipe(
                name = "Relleno",
                yield = Quantity.of("1", UnitOfMeasure.UNIT),
            ),
        )

        val parent = recipeRepository.save(
            Recipe(
                name = "Torta",
                yield = Quantity.of("1", UnitOfMeasure.UNIT),
                components = listOf(
                    SubRecipeComponent(
                        recipeId = base.id,
                        quantity = Quantity.of("1", UnitOfMeasure.UNIT),
                    ),
                ),
            ),
        )

        val found = recipeRepository.findById(parent.id)

        assertNotNull(found)
        assertEquals(1, found.components.size)
    }

    @Test
    fun `should update recipe and replace components`() {
        val ingredient = ingredientRepository.save(
            Ingredient(
                name = "Azucar",
                unit = UnitOfMeasure.GRAM,
                costPerUnit = Money.of("0.02"),
            ),
        )

        val saved = recipeRepository.save(
            Recipe(
                name = "Mermelada",
                yield = Quantity.of("1", UnitOfMeasure.UNIT),
            ),
        )

        val updated = recipeRepository.save(
            saved.copy(
                name = "Mermelada premium",
                components = listOf(
                    IngredientComponent(
                        ingredientId = ingredient.id,
                        quantity = Quantity.of("200", UnitOfMeasure.GRAM),
                    ),
                ),
            ),
        )

        val found = recipeRepository.findById(updated.id)

        assertNotNull(found)
        assertEquals("Mermelada premium", found.name)
        assertEquals(1, found.components.size)
    }

    @Test
    fun `should return null for missing recipe`() {
        val result = recipeRepository.findById(99999L)

        assertNull(result)
    }

    @Test
    fun `should return recipes ordered by name`() {
        recipeRepository.save(
            Recipe(
                name = "Zeta",
                yield = Quantity.of("1", UnitOfMeasure.UNIT),
            ),
        )
        recipeRepository.save(
            Recipe(
                name = "Alfa",
                yield = Quantity.of("1", UnitOfMeasure.UNIT),
            ),
        )

        val all = recipeRepository.findAll()

        assertEquals(2, all.size)
        assertEquals("Alfa", all.first().name)
        assertEquals("Zeta", all.last().name)
    }
}
