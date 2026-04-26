package com.recipecostcalculator.ui.screens.recipes

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.recipecostcalculator.ingredient.domain.model.Ingredient
import com.recipecostcalculator.ingredient.application.usecase.GetAllIngredientsUseCase
import com.recipecostcalculator.recipe.application.usecase.AddComponentToRecipeCommand
import com.recipecostcalculator.recipe.application.usecase.CreateRecipeCommand
import com.recipecostcalculator.recipe.application.usecase.GetAllRecipesUseCase
import com.recipecostcalculator.recipe.domain.model.IngredientComponent
import com.recipecostcalculator.recipe.domain.model.Quantity
import com.recipecostcalculator.recipe.domain.model.Recipe
import com.recipecostcalculator.recipe.domain.model.SubRecipeComponent
import com.recipecostcalculator.recipe.domain.model.UnitOfMeasure
import com.recipecostcalculator.ui.components.parseDecimalOrNull

class RecipePresenter(
    private val getAllIngredients: GetAllIngredientsUseCase,
    private val getAllRecipes: GetAllRecipesUseCase,
    private val createRecipe: (CreateRecipeCommand) -> Unit,
    private val addComponentToRecipe: (AddComponentToRecipeCommand) -> Unit,
) {
    var recipes by mutableStateOf(emptyList<Recipe>())
        private set

    var ingredients by mutableStateOf(emptyList<Ingredient>())
        private set

    var recipeNameInput by mutableStateOf("")
        private set

    var recipeYieldInput by mutableStateOf("1")
        private set

    var selectedRecipeId by mutableStateOf<Long?>(null)
        private set

    var selectedIngredientId by mutableStateOf<Long?>(null)
        private set

    var selectedSubRecipeId by mutableStateOf<Long?>(null)
        private set

    var componentQuantityInput by mutableStateOf("1")
        private set

    var componentUnit by mutableStateOf(UnitOfMeasure.UNIT)
        private set

    var message by mutableStateOf<String?>(null)
        private set

    init {
        refresh()
    }

    fun updateRecipeNameInput(value: String) {
        recipeNameInput = value
    }

    fun updateRecipeYieldInput(value: String) {
        recipeYieldInput = value
    }

    fun updateComponentQuantityInput(value: String) {
        componentQuantityInput = value
    }

    fun updateComponentUnit(value: UnitOfMeasure) {
        componentUnit = value
    }

    fun selectRecipe(recipe: Recipe) {
        selectedRecipeId = recipe.id
    }

    fun selectIngredient(ingredient: Ingredient) {
        selectedIngredientId = ingredient.id
        selectedSubRecipeId = null
        componentUnit = ingredient.unit
    }

    fun selectSubRecipe(recipe: Recipe) {
        selectedSubRecipeId = recipe.id
        selectedIngredientId = null
        componentUnit = UnitOfMeasure.UNIT
    }

    fun currentSelectedRecipe(): Recipe? {
        val currentId = selectedRecipeId ?: return null
        return recipes.firstOrNull { it.id == currentId }
    }

    fun currentSelectedIngredient(): Ingredient? {
        val currentId = selectedIngredientId ?: return null
        return ingredients.firstOrNull { it.id == currentId }
    }

    fun currentSelectedSubRecipe(): Recipe? {
        val currentId = selectedSubRecipeId ?: return null
        return recipes.firstOrNull { it.id == currentId }
    }

    fun createRecipe() {
        val yieldValue = parseDecimalOrNull(recipeYieldInput)
        if (recipeNameInput.isBlank() || yieldValue == null) {
            message = "Completa nombre y rendimiento valido"
            return
        }

        createRecipe(
            CreateRecipeCommand(
                name = recipeNameInput,
                yield = Quantity.of(yieldValue, UnitOfMeasure.UNIT),
            ),
        )

        recipeNameInput = ""
        recipeYieldInput = "1"
        message = "Receta creada"
        refresh()
    }

    fun addIngredientComponent() {
        val recipeId = selectedRecipeId
        val ingredientId = selectedIngredientId
        val quantityValue = parseDecimalOrNull(componentQuantityInput)
        if (recipeId == null || ingredientId == null || quantityValue == null) {
            message = "Selecciona receta, ingrediente y cantidad valida"
            return
        }

        addComponentToRecipe(
            AddComponentToRecipeCommand(
                recipeId = recipeId,
                component = IngredientComponent(
                    ingredientId = ingredientId,
                    quantity = Quantity.of(quantityValue, componentUnit),
                ),
            ),
        )

        message = "Ingrediente agregado"
        refresh()
    }

    fun addSubRecipeComponent() {
        val recipeId = selectedRecipeId
        val subRecipeId = selectedSubRecipeId
        val quantityValue = parseDecimalOrNull(componentQuantityInput)
        if (recipeId == null || subRecipeId == null || quantityValue == null) {
            message = "Selecciona receta, sub-receta y cantidad valida"
            return
        }

        addComponentToRecipe(
            AddComponentToRecipeCommand(
                recipeId = recipeId,
                component = SubRecipeComponent(
                    recipeId = subRecipeId,
                    quantity = Quantity.of(quantityValue, UnitOfMeasure.UNIT),
                ),
            ),
        )

        message = "Sub-receta agregada"
        refresh()
    }

    private fun refresh() {
        recipes = getAllRecipes.execute()
        ingredients = getAllIngredients.execute()
    }
}
