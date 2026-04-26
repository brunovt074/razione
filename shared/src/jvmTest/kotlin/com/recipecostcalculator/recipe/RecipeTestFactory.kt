package com.recipecostcalculator.recipe

import com.recipecostcalculator.recipe.domain.model.Quantity
import com.recipecostcalculator.recipe.domain.model.Recipe
import com.recipecostcalculator.recipe.domain.model.RecipeComponent
import com.recipecostcalculator.recipe.domain.model.UnitOfMeasure

object RecipeTestFactory {
    fun create(
        id: Long = 0,
        name: String = "Masa",
        yield: Quantity = Quantity.of("1", UnitOfMeasure.UNIT),
        components: List<RecipeComponent> = emptyList(),
    ): Recipe {
        return Recipe(
            id = id,
            name = name,
            yield = yield,
            components = components,
        )
    }
}
