package com.recipecostcalculator.application.usecase.costing

import com.recipecostcalculator.domain.repository.RecipeRepository
import com.recipecostcalculator.domain.service.CostCalculatorService
import com.recipecostcalculator.domain.service.CostResult

class CalculateRecipeCostUseCase(
    private val recipeRepository: RecipeRepository,
    private val costCalculatorService: CostCalculatorService,
) {
    data class Query(
        val recipeId: Long,
    )

    operator fun invoke(query: Query): CostResult {
        val recipe = recipeRepository.findById(query.recipeId)
            ?: throw IllegalArgumentException("Receta no encontrada: ${query.recipeId}")
        return costCalculatorService.calculate(recipe)
    }
}
