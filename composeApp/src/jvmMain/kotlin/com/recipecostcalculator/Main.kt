package com.recipecostcalculator

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.recipecostcalculator.costing.application.usecase.CalculateRecipeCostQuery
import com.recipecostcalculator.costing.application.usecase.CalculateRecipeCostUseCase
import com.recipecostcalculator.costing.domain.service.CostCalculatorService
import com.recipecostcalculator.costing.domain.service.CostResult
import com.recipecostcalculator.ingredient.application.usecase.CreateIngredientCommand
import com.recipecostcalculator.ingredient.application.usecase.CreateIngredientUseCase
import com.recipecostcalculator.ingredient.application.usecase.GetAllIngredientsUseCase
import com.recipecostcalculator.ingredient.application.usecase.UpdateIngredientCostCommand
import com.recipecostcalculator.ingredient.application.usecase.UpdateIngredientCostUseCase
import com.recipecostcalculator.ingredient.domain.repository.IngredientRepository
import com.recipecostcalculator.infrastructure.persistence.sqlite.SQLiteDatabase
import com.recipecostcalculator.infrastructure.persistence.sqlite.repository.SQLiteIngredientRepository
import com.recipecostcalculator.infrastructure.persistence.sqlite.repository.SQLiteRecipeRepository
import com.recipecostcalculator.recipe.application.usecase.AddComponentToRecipeCommand
import com.recipecostcalculator.recipe.application.usecase.AddComponentToRecipeUseCase
import com.recipecostcalculator.recipe.application.usecase.CreateRecipeCommand
import com.recipecostcalculator.recipe.application.usecase.CreateRecipeUseCase
import com.recipecostcalculator.recipe.application.usecase.GetAllRecipesUseCase
import com.recipecostcalculator.recipe.domain.repository.RecipeRepository
import com.recipecostcalculator.ui.screens.costing.CostPresenter
import com.recipecostcalculator.ui.screens.costing.CostScreen
import com.recipecostcalculator.ui.screens.ingredients.IngredientPresenter
import com.recipecostcalculator.ui.screens.ingredients.IngredientScreen
import com.recipecostcalculator.ui.screens.recipes.RecipePresenter
import com.recipecostcalculator.ui.screens.recipes.RecipeScreen

fun main() = application {
    val database = remember { SQLiteDatabase() }
    val ingredientRepository: IngredientRepository = remember { SQLiteIngredientRepository(database) }
    val recipeRepository: RecipeRepository = remember { SQLiteRecipeRepository(database) }

    val costCalculatorService = remember {
        CostCalculatorService(
            ingredientRepository = ingredientRepository,
            recipeRepository = recipeRepository,
        )
    }

    val createIngredientUseCase = remember { CreateIngredientUseCase(ingredientRepository) }
    val updateIngredientCostUseCase = remember { UpdateIngredientCostUseCase(ingredientRepository) }
    val getAllIngredientsUseCase = remember { GetAllIngredientsUseCase(ingredientRepository) }
    val createRecipeUseCase = remember { CreateRecipeUseCase(recipeRepository) }
    val addComponentToRecipeUseCase = remember { AddComponentToRecipeUseCase(recipeRepository) }
    val getAllRecipesUseCase = remember { GetAllRecipesUseCase(recipeRepository) }
    val calculateRecipeCostUseCase = remember {
        CalculateRecipeCostUseCase(
            recipeRepository = recipeRepository,
            costCalculatorService = costCalculatorService,
        )
    }

    val createIngredient = remember {
        { command: CreateIngredientCommand ->
            createIngredientUseCase.execute(command)
            Unit
        }
    }
    val updateIngredientCost = remember {
        { command: UpdateIngredientCostCommand ->
            updateIngredientCostUseCase.execute(command)
            Unit
        }
    }
    val createRecipe = remember {
        { command: CreateRecipeCommand ->
            createRecipeUseCase.execute(command)
            Unit
        }
    }
    val addComponentToRecipe = remember {
        { command: AddComponentToRecipeCommand ->
            addComponentToRecipeUseCase.execute(command)
            Unit
        }
    }
    val calculateRecipeCost = remember {
        { query: CalculateRecipeCostQuery ->
            calculateRecipeCostUseCase.execute(query)
        }
    }

    val ingredientPresenter = remember {
        IngredientPresenter(
            getAllIngredients = getAllIngredientsUseCase,
            createIngredient = createIngredient,
            updateIngredientCost = updateIngredientCost,
        )
    }

    val recipePresenter = remember {
        RecipePresenter(
            getAllIngredients = getAllIngredientsUseCase,
            getAllRecipes = getAllRecipesUseCase,
            createRecipe = createRecipe,
            addComponentToRecipe = addComponentToRecipe,
        )
    }

    val costPresenter = remember {
        CostPresenter(
            getAllRecipes = getAllRecipesUseCase,
            calculateRecipeCost = calculateRecipeCost,
        )
    }

    Window(
        onCloseRequest = ::exitApplication,
        title = "Recipe Cost Calculator",
    ) {
        MaterialTheme {
            Surface(modifier = Modifier.fillMaxSize()) {
                AppContent(
                    ingredientPresenter = ingredientPresenter,
                    recipePresenter = recipePresenter,
                    costPresenter = costPresenter,
                )
            }
        }
    }
}

@Composable
private fun AppContent(
    ingredientPresenter: IngredientPresenter,
    recipePresenter: RecipePresenter,
    costPresenter: CostPresenter,
) {
    var selectedScreen by remember { mutableStateOf(AppScreen.INGREDIENTS) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        NavigationBar(
            selectedScreen = selectedScreen,
            onSelectScreen = { selectedScreen = it },
        )

        when (selectedScreen) {
            AppScreen.INGREDIENTS -> IngredientScreen(
                presenter = ingredientPresenter,
            )

            AppScreen.RECIPES -> RecipeScreen(
                presenter = recipePresenter,
            )

            AppScreen.COSTS -> CostScreen(
                presenter = costPresenter,
            )
        }
    }
}

@Composable
private fun NavigationBar(
    selectedScreen: AppScreen,
    onSelectScreen: (AppScreen) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        AppScreen.entries.forEach { screen ->
            val label = when (screen) {
                AppScreen.INGREDIENTS -> "Ingredientes"
                AppScreen.RECIPES -> "Recetas"
                AppScreen.COSTS -> "Costos"
            }
            Button(onClick = { onSelectScreen(screen) }) {
                Text(
                    if (screen == selectedScreen) {
                        "[$label]"
                    } else {
                        label
                    },
                )
            }
        }
    }
}
