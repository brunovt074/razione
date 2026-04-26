package com.recipecostcalculator.costing.domain.service

import com.recipecostcalculator.financial.domain.model.Money
import com.recipecostcalculator.ingredient.domain.repository.IngredientRepository
import com.recipecostcalculator.recipe.domain.model.IngredientComponent
import com.recipecostcalculator.recipe.domain.model.Recipe
import com.recipecostcalculator.recipe.domain.model.SubRecipeComponent
import com.recipecostcalculator.recipe.domain.repository.RecipeRepository
import java.math.BigDecimal
import java.math.RoundingMode

class CostCalculatorService(
    private val ingredientRepository: IngredientRepository,
    private val recipeRepository: RecipeRepository,
) {

    fun calculate(recipe: Recipe): CostResult {
        require(recipe.id > 0) { "La receta debe tener id para calcular costos" }

        val calculated = calculateRecipe(recipe, scaleFactor = BigDecimal.ONE, visitedRecipeIds = mutableSetOf())
        val totalCost = calculated.totalCost
        val perUnitFactor = BigDecimal.ONE.divide(recipe.yield.value, 6, RoundingMode.HALF_UP)
        val costPerUnit = totalCost * perUnitFactor

        val breakdownList = calculated.breakdown.values
            .map {
                IngredientCostBreakdown(
                    ingredientId = it.ingredientId,
                    ingredientName = it.ingredientName,
                    quantity = it.quantity,
                    cost = it.cost,
                )
            }
            .sortedBy { it.ingredientName.lowercase() }

        return CostResult(
            recipeId = recipe.id,
            recipeName = recipe.name,
            totalCost = totalCost,
            costPerUnit = costPerUnit,
            breakdown = breakdownList,
        )
    }

    private fun calculateRecipe(
        recipe: Recipe,
        scaleFactor: BigDecimal,
        visitedRecipeIds: MutableSet<Long>,
    ): IntermediateCostResult {
        val recipeId = recipe.id
        check(!visitedRecipeIds.contains(recipeId)) {
            "Se detecto una dependencia circular en la receta ${recipe.name}"
        }

        visitedRecipeIds.add(recipeId)
        var totalCost: Money? = null
        val breakdown = linkedMapOf<Long, IngredientBreakdownAccumulator>()

        try {
            for (component in recipe.components) {
                when (component) {
                    is IngredientComponent -> {
                        val ingredient = ingredientRepository.findById(component.ingredientId)
                            ?: throw IllegalStateException("Ingrediente ${component.ingredientId} no encontrado")
                        val ingredientId = ingredient.id
                        check(ingredientId > 0) { "Ingrediente sin id persistido" }

                        val effectiveQuantity = component.quantity * scaleFactor
                        val cost = ingredient.costFor(effectiveQuantity)
                        totalCost = addMoney(totalCost, cost)

                        val existing = breakdown[ingredientId]
                        breakdown[ingredientId] = if (existing == null) {
                            IngredientBreakdownAccumulator(
                                ingredientId = ingredientId,
                                ingredientName = ingredient.name,
                                quantity = effectiveQuantity.to(ingredient.unit),
                                cost = cost,
                            )
                        } else {
                            existing.copy(
                                quantity = existing.quantity + effectiveQuantity,
                                cost = existing.cost + cost,
                            )
                        }
                    }

                    is SubRecipeComponent -> {
                        val subRecipe = recipeRepository.findById(component.recipeId)
                            ?: throw IllegalStateException("Receta ${component.recipeId} no encontrada")

                        val desiredOutput = component.quantity * scaleFactor
                        val normalizedDesired = desiredOutput.to(subRecipe.yield.unit)
                        val nestedScaleFactor = normalizedDesired.value.divide(subRecipe.yield.value, 6, RoundingMode.HALF_UP)

                        val nested = calculateRecipe(
                            recipe = subRecipe,
                            scaleFactor = nestedScaleFactor,
                            visitedRecipeIds = visitedRecipeIds,
                        )
                        totalCost = addMoney(totalCost, nested.totalCost)

                        mergeBreakdowns(target = breakdown, source = nested.breakdown)
                    }
                }
            }
        } finally {
            visitedRecipeIds.remove(recipeId)
        }

        return IntermediateCostResult(totalCost = totalCost ?: Money.zero(), breakdown = breakdown)
    }

    private fun addMoney(current: Money?, value: Money): Money {
        if (current == null) {
            return value
        }
        if (value.amount.signum() == 0) {
            return current
        }
        if (current.amount.signum() == 0 && current.currency != value.currency) {
            return Money.of(value.amount, value.currency)
        }
        return current + value
    }

    private fun mergeBreakdowns(
        target: LinkedHashMap<Long, IngredientBreakdownAccumulator>,
        source: Map<Long, IngredientBreakdownAccumulator>,
    ) {
        source.values.forEach { item ->
            val existing = target[item.ingredientId]
            target[item.ingredientId] = if (existing == null) {
                item.copy()
            } else {
                existing.copy(
                    quantity = existing.quantity + item.quantity,
                    cost = existing.cost + item.cost,
                )
            }
        }
    }
}
