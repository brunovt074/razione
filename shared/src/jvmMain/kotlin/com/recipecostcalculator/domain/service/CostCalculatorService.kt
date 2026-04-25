package com.recipecostcalculator.domain.service

import com.recipecostcalculator.domain.model.recipe.IngredientComponent
import com.recipecostcalculator.domain.model.recipe.Recipe
import com.recipecostcalculator.domain.model.recipe.SubRecipeComponent
import com.recipecostcalculator.domain.model.valueobject.Money
import com.recipecostcalculator.domain.model.valueobject.Quantity
import com.recipecostcalculator.domain.repository.IngredientRepository
import com.recipecostcalculator.domain.repository.RecipeRepository
import java.math.BigDecimal
import java.math.RoundingMode

class CostCalculatorService(
    private val ingredientRepository: IngredientRepository,
    private val recipeRepository: RecipeRepository,
) {

    fun calculate(recipe: Recipe): CostResult {
        requireNotNull(recipe.id) { "La receta debe tener id para calcular costos" }

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
    ): IntermediateResult {
        val recipeId = requireNotNull(recipe.id) { "La receta debe tener id" }
        check(!visitedRecipeIds.contains(recipeId)) {
            "Se detecto una dependencia circular en la receta ${recipe.name}"
        }

        visitedRecipeIds.add(recipeId)
        var totalCost: Money? = null
        val breakdown = linkedMapOf<Long, MutableBreakdown>()

        try {
            for (component in recipe.components) {
                when (component) {
                    is IngredientComponent -> {
                        val ingredient = ingredientRepository.findById(component.ingredientId)
                            ?: throw IllegalStateException("Ingrediente ${component.ingredientId} no encontrado")
                        val ingredientId = requireNotNull(ingredient.id) { "Ingrediente sin id persistido" }
                        val effectiveQuantity = component.quantity * scaleFactor
                        val cost = ingredient.costFor(effectiveQuantity)
                        totalCost = addMoney(totalCost, cost)

                        val existing = breakdown[ingredientId]
                        if (existing == null) {
                            breakdown[ingredientId] = MutableBreakdown(
                                ingredientId = ingredientId,
                                ingredientName = ingredient.name,
                                quantity = effectiveQuantity.to(ingredient.unit),
                                cost = cost,
                            )
                        } else {
                            existing.quantity = existing.quantity + effectiveQuantity
                            existing.cost = existing.cost + cost
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

        return IntermediateResult(totalCost = totalCost ?: Money.zero(), breakdown = breakdown)
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
        target: LinkedHashMap<Long, MutableBreakdown>,
        source: Map<Long, MutableBreakdown>,
    ) {
        source.values.forEach { item ->
            val existing = target[item.ingredientId]
            if (existing == null) {
                target[item.ingredientId] = item.copy()
            } else {
                existing.quantity = existing.quantity + item.quantity
                existing.cost = existing.cost + item.cost
            }
        }
    }

    private data class IntermediateResult(
        val totalCost: Money,
        val breakdown: LinkedHashMap<Long, MutableBreakdown>,
    )

    private data class MutableBreakdown(
        val ingredientId: Long,
        val ingredientName: String,
        var quantity: Quantity,
        var cost: Money,
    )
}
