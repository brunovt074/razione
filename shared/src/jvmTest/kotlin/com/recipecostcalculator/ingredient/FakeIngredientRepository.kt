package com.recipecostcalculator.ingredient

import com.recipecostcalculator.ingredient.domain.model.Ingredient
import com.recipecostcalculator.ingredient.domain.repository.IngredientRepository

class FakeIngredientRepository : IngredientRepository {
    private val store = mutableMapOf<Long, Ingredient>()
    private var nextId = 1L

    override fun save(ingredient: Ingredient): Ingredient {
        val persisted = if (ingredient.id == 0L) {
            ingredient.copy(id = nextId++)
        } else {
            ingredient
        }
        store[persisted.id] = persisted
        return persisted
    }

    override fun findById(id: Long): Ingredient? = store[id]

    override fun findAll(): List<Ingredient> = store.values.sortedBy { it.name }

    fun clear() {
        store.clear()
        nextId = 1L
    }
}
