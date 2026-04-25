package com.recipecostcalculator.domain.repository

import com.recipecostcalculator.domain.model.recipe.Recipe

interface RecipeRepository {
    fun save(recipe: Recipe): Recipe
    fun findById(id: Long): Recipe?
    fun findAll(): List<Recipe>
}
