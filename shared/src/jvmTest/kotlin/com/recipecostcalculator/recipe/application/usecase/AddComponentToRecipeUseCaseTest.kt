package com.recipecostcalculator.recipe.application.usecase

import com.recipecostcalculator.recipe.FakeRecipeRepository
import com.recipecostcalculator.recipe.RecipeTestFactory
import com.recipecostcalculator.recipe.domain.model.IngredientComponent
import com.recipecostcalculator.recipe.domain.model.Quantity
import com.recipecostcalculator.recipe.domain.model.SubRecipeComponent
import com.recipecostcalculator.recipe.domain.model.UnitOfMeasure
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AddComponentToRecipeUseCaseTest {
    private val repository = FakeRecipeRepository()
    private val useCase = AddComponentToRecipeUseCase(repository)

    @Test
    fun `should add ingredient component to recipe`() {
        val recipe = repository.save(RecipeTestFactory.create(name = "Masa"))

        val updated = useCase.execute(
            AddComponentToRecipeCommand(
                recipeId = recipe.id,
                component = IngredientComponent(
                    ingredientId = 10L,
                    quantity = Quantity.of("500", UnitOfMeasure.GRAM),
                ),
            ),
        )

        assertEquals(1, updated.components.size)
    }

    @Test
    fun `should throw when recipe includes itself`() {
        val recipe = repository.save(RecipeTestFactory.create(name = "Base"))

        assertFailsWith<IllegalArgumentException> {
            useCase.execute(
                AddComponentToRecipeCommand(
                    recipeId = recipe.id,
                    component = SubRecipeComponent(
                        recipeId = recipe.id,
                        quantity = Quantity.of("1", UnitOfMeasure.UNIT),
                    ),
                ),
            )
        }
    }
}
