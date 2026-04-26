package com.recipecostcalculator.recipe

import com.recipecostcalculator.recipe.domain.model.Recipe
import com.recipecostcalculator.recipe.domain.repository.RecipeRepository

class FakeRecipeRepository : RecipeRepository {
    private val store = mutableMapOf<Long, Recipe>()
    private var nextId = 1L

    override fun save(recipe: Recipe): Recipe {
        val persisted = if (recipe.id == 0L) {
            recipe.copy(id = nextId++)
        } else {
            recipe
        }
        store[persisted.id] = persisted
        return persisted
    }

    override fun findById(id: Long): Recipe? = store[id]

    override fun findAll(): List<Recipe> = store.values.sortedBy { it.name }

    fun clear() {
        store.clear()
        nextId = 1L
    }
}
