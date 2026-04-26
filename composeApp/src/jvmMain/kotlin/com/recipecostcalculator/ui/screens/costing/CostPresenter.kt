package com.recipecostcalculator.ui.screens.costing

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.recipecostcalculator.costing.application.dto.RecipeCostDto
import com.recipecostcalculator.costing.application.usecase.CalculateRecipeCostQuery
import com.recipecostcalculator.costing.domain.service.CostResult
import com.recipecostcalculator.recipe.application.usecase.GetAllRecipesUseCase
import com.recipecostcalculator.recipe.domain.model.Recipe

class CostPresenter(
    private val getAllRecipes: GetAllRecipesUseCase,
    private val calculateRecipeCost: (CalculateRecipeCostQuery) -> CostResult,
) {
    var recipes by mutableStateOf(emptyList<Recipe>())
        private set

    var selectedRecipeId by mutableStateOf<Long?>(null)
        private set

    var costResult by mutableStateOf<RecipeCostDto?>(null)
        private set

    var message by mutableStateOf<String?>(null)
        private set

    init {
        refresh()
        message = null
    }

    fun selectRecipe(recipe: Recipe) {
        selectedRecipeId = recipe.id
        message = null
    }

    fun currentSelectedRecipe(): Recipe? {
        val currentId = selectedRecipeId ?: return null
        return recipes.firstOrNull { it.id == currentId }
    }

    fun calculate() {
        val recipeId = selectedRecipeId
        if (recipeId == null) {
            message = "Selecciona una receta"
            return
        }

        val result = calculateRecipeCost(CalculateRecipeCostQuery(recipeId = recipeId))
        costResult = RecipeCostDto.from(result)
    }

    fun refresh() {
        recipes = getAllRecipes.execute()
        message = "Recetas recargadas"
    }
}
