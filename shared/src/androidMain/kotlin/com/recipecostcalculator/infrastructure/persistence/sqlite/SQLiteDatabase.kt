package com.recipecostcalculator.infrastructure.persistence.sqlite

class SQLiteDatabase(
    databasePath: String = "recipe-cost.db",
) {
    private val delegate = AndroidSQLiteDatabase(AndroidSQLiteRuntimeContext.requireContext())

    init {
        check(databasePath.isNotBlank()) { "El path de base no puede estar vacio" }
    }

    fun ingredientRepository(): com.recipecostcalculator.ingredient.domain.repository.IngredientRepository {
        return com.recipecostcalculator.infrastructure.persistence.sqlite.repository.AndroidSQLiteIngredientRepository(delegate)
    }

    fun recipeRepository(): com.recipecostcalculator.recipe.domain.repository.RecipeRepository {
        return com.recipecostcalculator.infrastructure.persistence.sqlite.repository.AndroidSQLiteRecipeRepository(delegate)
    }

    fun fixedCostRepository(): com.recipecostcalculator.fixedcost.domain.repository.FixedCostRepository {
        return com.recipecostcalculator.infrastructure.persistence.sqlite.repository.AndroidSQLiteFixedCostRepository(delegate)
    }

    fun pricingRepository(): com.recipecostcalculator.pricing.domain.repository.PricingRepository {
        return com.recipecostcalculator.infrastructure.persistence.sqlite.repository.AndroidSQLitePricingRepository(delegate)
    }
}
