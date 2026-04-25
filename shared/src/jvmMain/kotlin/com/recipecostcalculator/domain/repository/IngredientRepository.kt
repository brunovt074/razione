package com.recipecostcalculator.domain.repository

import com.recipecostcalculator.domain.model.ingredient.Ingredient

interface IngredientRepository {
    fun save(ingredient: Ingredient): Ingredient
    fun findById(id: Long): Ingredient?
    fun findAll(): List<Ingredient>
}
