package com.recipecostcalculator.costing.application.usecase

import com.recipecostcalculator.costing.domain.service.CostCalculatorService
import com.recipecostcalculator.financial.domain.model.Money
import com.recipecostcalculator.ingredient.FakeIngredientRepository
import com.recipecostcalculator.ingredient.IngredientTestFactory
import com.recipecostcalculator.recipe.FakeRecipeRepository
import com.recipecostcalculator.recipe.RecipeTestFactory
import com.recipecostcalculator.recipe.domain.model.IngredientComponent
import com.recipecostcalculator.recipe.domain.model.Quantity
import com.recipecostcalculator.recipe.domain.model.SubRecipeComponent
import com.recipecostcalculator.recipe.domain.model.UnitOfMeasure
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class CalculateRecipeCostUseCaseTest {
    private val ingredientRepository = FakeIngredientRepository()
    private val recipeRepository = FakeRecipeRepository()
    private val service = CostCalculatorService(ingredientRepository, recipeRepository)
    private val useCase = CalculateRecipeCostUseCase(recipeRepository, service)

    @Test
    fun `should calculate direct ingredient cost`() {
        val flour = ingredientRepository.save(
            IngredientTestFactory.create(
                name = "Harina",
                costPerUnit = Money.of("0.01"),
            ),
        )

        val recipe = recipeRepository.save(
            RecipeTestFactory.create(
                name = "Pan",
                yield = Quantity.of("2", UnitOfMeasure.UNIT),
                components = listOf(
                    IngredientComponent(
                        ingredientId = flour.id,
                        quantity = Quantity.of("500", UnitOfMeasure.GRAM),
                    ),
                ),
            ),
        )

        val result = useCase.execute(CalculateRecipeCostQuery(recipeId = recipe.id))

        assertEquals("5", result.totalCost.amount.toPlainString())
        assertTrue(result.breakdown.isNotEmpty())
    }

    @Test
    fun `should include sub recipe cost recursively`() {
        val sugar = ingredientRepository.save(
            IngredientTestFactory.create(
                name = "Azucar",
                costPerUnit = Money.of("0.02"),
            ),
        )

        val syrup = recipeRepository.save(
            RecipeTestFactory.create(
                name = "Almibar",
                yield = Quantity.of("1", UnitOfMeasure.UNIT),
                components = listOf(
                    IngredientComponent(
                        ingredientId = sugar.id,
                        quantity = Quantity.of("100", UnitOfMeasure.GRAM),
                    ),
                ),
            ),
        )

        val cake = recipeRepository.save(
            RecipeTestFactory.create(
                name = "Torta",
                yield = Quantity.of("1", UnitOfMeasure.UNIT),
                components = listOf(
                    SubRecipeComponent(
                        recipeId = syrup.id,
                        quantity = Quantity.of("1", UnitOfMeasure.UNIT),
                    ),
                ),
            ),
        )

        val result = useCase.execute(CalculateRecipeCostQuery(recipeId = cake.id))

        assertEquals("2", result.totalCost.amount.toPlainString())
    }

    @Test
    fun `should throw when recipe does not exist`() {
        assertFailsWith<IllegalArgumentException> {
            useCase.execute(CalculateRecipeCostQuery(recipeId = 999L))
        }
    }

    @Test
    fun `should detect recursive cycle in recipes`() {
        val base = recipeRepository.save(
            RecipeTestFactory.create(
                name = "Base",
                yield = Quantity.of("1", UnitOfMeasure.UNIT),
            ),
        )

        val dependent = recipeRepository.save(
            RecipeTestFactory.create(
                name = "Dependiente",
                yield = Quantity.of("1", UnitOfMeasure.UNIT),
                components = listOf(
                    SubRecipeComponent(
                        recipeId = base.id,
                        quantity = Quantity.of("1", UnitOfMeasure.UNIT),
                    ),
                ),
            ),
        )

        recipeRepository.save(
            base.copy(
                components = listOf(
                    SubRecipeComponent(
                        recipeId = dependent.id,
                        quantity = Quantity.of("1", UnitOfMeasure.UNIT),
                    ),
                ),
            ),
        )

        assertFailsWith<IllegalStateException> {
            useCase.execute(CalculateRecipeCostQuery(recipeId = base.id))
        }
    }
}
