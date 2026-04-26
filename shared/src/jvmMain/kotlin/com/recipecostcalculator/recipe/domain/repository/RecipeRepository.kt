package com.recipecostcalculator.recipe.domain.repository

import com.recipecostcalculator.recipe.domain.model.Recipe

interface RecipeRepository {
    fun save(recipe: Recipe): Recipe
    fun findById(id: Long): Recipe?
    fun findAll(): List<Recipe>
}
