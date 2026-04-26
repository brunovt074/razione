package com.recipecostcalculator.ingredient.application.usecase

import com.recipecostcalculator.financial.domain.model.Money
import com.recipecostcalculator.ingredient.FakeIngredientRepository
import com.recipecostcalculator.ingredient.IngredientTestFactory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class UpdateIngredientCostUseCaseTest {
    private val repository = FakeIngredientRepository()
    private val useCase = UpdateIngredientCostUseCase(repository)

    @Test
    fun `should update ingredient cost`() {
        val saved = repository.save(IngredientTestFactory.create())

        val updated = useCase.execute(
            UpdateIngredientCostCommand(
                ingredientId = saved.id,
                costPerUnit = Money.of("0.050"),
            ),
        )

        assertEquals("0.05", updated.costPerUnit.amount.toPlainString())
    }

    @Test
    fun `should throw when ingredient does not exist`() {
        assertFailsWith<IllegalArgumentException> {
            useCase.execute(
                UpdateIngredientCostCommand(
                    ingredientId = 999L,
                    costPerUnit = Money.of("1.0"),
                ),
            )
        }
    }
}
