package com.recipecostcalculator.ingredient.application.usecase

import com.recipecostcalculator.financial.domain.model.Money
import com.recipecostcalculator.ingredient.FakeIngredientRepository
import com.recipecostcalculator.recipe.domain.model.UnitOfMeasure
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CreateIngredientUseCaseTest {
    private val repository = FakeIngredientRepository()
    private val useCase = CreateIngredientUseCase(repository)

    @Test
    fun `should create ingredient and assign id`() {
        val result = useCase.execute(
            CreateIngredientCommand(
                name = "Harina 000",
                unit = UnitOfMeasure.GRAM,
                costPerUnit = Money.of("0.012"),
            ),
        )

        assertTrue(result.id > 0)
        assertEquals("Harina 000", result.name)
    }
}
