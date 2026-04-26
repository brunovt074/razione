package com.recipecostcalculator.recipe.application.usecase

import com.recipecostcalculator.recipe.FakeRecipeRepository
import com.recipecostcalculator.recipe.domain.model.Quantity
import com.recipecostcalculator.recipe.domain.model.UnitOfMeasure
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CreateRecipeUseCaseTest {
    private val repository = FakeRecipeRepository()
    private val useCase = CreateRecipeUseCase(repository)

    @Test
    fun `should create recipe with assigned id`() {
        val result = useCase.execute(
            CreateRecipeCommand(
                name = "Pan",
                yield = Quantity.of("2", UnitOfMeasure.UNIT),
            ),
        )

        assertTrue(result.id > 0)
        assertEquals("Pan", result.name)
    }
}
