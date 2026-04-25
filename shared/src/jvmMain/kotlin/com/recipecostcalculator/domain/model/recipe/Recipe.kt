package com.recipecostcalculator.domain.model.recipe

import com.recipecostcalculator.domain.model.valueobject.Quantity
import com.recipecostcalculator.domain.model.valueobject.UnitCategory

data class Recipe(
    val id: Long? = null,
    val name: String,
    val yield: Quantity,
    val components: List<RecipeComponent> = emptyList(),
) {
    init {
        require(name.isNotBlank()) { "El nombre de la receta es obligatorio" }
        require(yield.value.signum() > 0) { "El rendimiento debe ser mayor a cero" }
        require(yield.unit.category == UnitCategory.COUNT) {
            "El rendimiento de receta se expresa en unidades"
        }
    }

    fun withComponent(component: RecipeComponent): Recipe {
        require(component.quantity.value.signum() > 0) { "La cantidad del componente debe ser mayor a cero" }
        return copy(components = components + component)
    }
}
