package com.recipecostcalculator.recipe.application.usecase

import com.recipecostcalculator.recipe.domain.model.RecipeComponent

data class AddComponentToRecipeCommand(
    val recipeId: Long,
    val component: RecipeComponent,
)
