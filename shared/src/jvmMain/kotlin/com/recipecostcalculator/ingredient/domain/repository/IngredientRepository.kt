package com.recipecostcalculator.ingredient.domain.repository

import com.recipecostcalculator.ingredient.domain.model.Ingredient

interface IngredientRepository {
    fun save(ingredient: Ingredient): Ingredient
    fun findById(id: Long): Ingredient?
    fun findAll(): List<Ingredient>
}
