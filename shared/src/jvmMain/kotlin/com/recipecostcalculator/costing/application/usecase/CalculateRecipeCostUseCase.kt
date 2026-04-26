package com.recipecostcalculator.costing.application.usecase

import com.recipecostcalculator.costing.domain.service.CostCalculatorService
import com.recipecostcalculator.costing.domain.service.CostResult
import com.recipecostcalculator.recipe.domain.repository.RecipeRepository

class CalculateRecipeCostUseCase(
    private val recipeRepository: RecipeRepository,
    private val costCalculatorService: CostCalculatorService,
) {
    fun execute(query: CalculateRecipeCostQuery): CostResult {
        val recipe = recipeRepository.findById(query.recipeId)
            ?: throw IllegalArgumentException("Receta no encontrada: ${query.recipeId}")
        return costCalculatorService.calculate(recipe)
    }
}
