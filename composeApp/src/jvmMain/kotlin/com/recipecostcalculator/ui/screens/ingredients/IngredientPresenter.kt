package com.recipecostcalculator.ui.screens.ingredients

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.recipecostcalculator.financial.domain.model.Money
import com.recipecostcalculator.ingredient.application.usecase.CreateIngredientCommand
import com.recipecostcalculator.ingredient.application.usecase.GetAllIngredientsUseCase
import com.recipecostcalculator.ingredient.application.usecase.UpdateIngredientCostCommand
import com.recipecostcalculator.ingredient.domain.model.Ingredient
import com.recipecostcalculator.recipe.domain.model.UnitOfMeasure
import com.recipecostcalculator.ui.components.parseDecimalOrNull

class IngredientPresenter(
    private val getAllIngredients: GetAllIngredientsUseCase,
    private val createIngredient: (CreateIngredientCommand) -> Unit,
    private val updateIngredientCost: (UpdateIngredientCostCommand) -> Unit,
) {
    var ingredients by mutableStateOf(emptyList<Ingredient>())
        private set

    var nameInput by mutableStateOf("")
        private set

    var costInput by mutableStateOf("")
        private set

    var selectedUnit by mutableStateOf(UnitOfMeasure.GRAM)
        private set

    var selectedIngredientId by mutableStateOf<Long?>(null)
        private set

    var updatedCostInput by mutableStateOf("")
        private set

    var message by mutableStateOf<String?>(null)
        private set

    init {
        refresh()
    }

    fun updateNameInput(value: String) {
        nameInput = value
    }

    fun updateCostInput(value: String) {
        costInput = value
    }

    fun updateSelectedUnit(value: UnitOfMeasure) {
        selectedUnit = value
    }

    fun updateUpdatedCostInput(value: String) {
        updatedCostInput = value
    }

    fun selectIngredient(ingredient: Ingredient) {
        selectedIngredientId = ingredient.id
        updatedCostInput = ingredient.costPerUnit.amount.toPlainString()
    }

    fun createIngredient() {
        val cost = parseDecimalOrNull(costInput)
        if (nameInput.isBlank() || cost == null) {
            message = "Completa nombre y costo valido"
            return
        }

        createIngredient(
            CreateIngredientCommand(
                name = nameInput,
                unit = selectedUnit,
                costPerUnit = Money.of(cost),
            ),
        )
        nameInput = ""
        costInput = ""
        message = "Ingrediente creado"
        refresh()
    }

    fun updateIngredientCost() {
        val ingredientId = selectedIngredientId
        val cost = parseDecimalOrNull(updatedCostInput)
        if (ingredientId == null || cost == null) {
            message = "Selecciona ingrediente y costo valido"
            return
        }

        updateIngredientCost(
            UpdateIngredientCostCommand(
                ingredientId = ingredientId,
                costPerUnit = Money.of(cost),
            ),
        )
        message = "Costo actualizado"
        refresh()
    }

    fun currentSelectedIngredient(): Ingredient? {
        val currentId = selectedIngredientId ?: return null
        return ingredients.firstOrNull { it.id == currentId }
    }

    private fun refresh() {
        ingredients = getAllIngredients.execute()
    }
}
