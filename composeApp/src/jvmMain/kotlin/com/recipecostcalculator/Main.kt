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
import com.recipecostcalculator.application.usecase.costing.CalculateRecipeCostUseCase
import com.recipecostcalculator.application.usecase.ingredient.CreateIngredientUseCase
import com.recipecostcalculator.application.usecase.ingredient.UpdateIngredientCostUseCase
import com.recipecostcalculator.application.usecase.recipe.AddComponentToRecipeUseCase
import com.recipecostcalculator.application.usecase.recipe.CreateRecipeUseCase
import com.recipecostcalculator.domain.repository.IngredientRepository
import com.recipecostcalculator.domain.repository.RecipeRepository
import com.recipecostcalculator.domain.service.CostCalculatorService
import com.recipecostcalculator.infrastructure.persistence.sqlite.SQLiteDatabase
import com.recipecostcalculator.infrastructure.persistence.sqlite.repository.SQLiteIngredientRepository
import com.recipecostcalculator.infrastructure.persistence.sqlite.repository.SQLiteRecipeRepository
import com.recipecostcalculator.ui.screens.costing.CostScreen
import com.recipecostcalculator.ui.screens.ingredients.IngredientScreen
import com.recipecostcalculator.ui.screens.recipes.RecipeScreen

private enum class AppScreen {
    INGREDIENTS,
    RECIPES,
    COSTS,
}

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
    val createRecipeUseCase = remember { CreateRecipeUseCase(recipeRepository) }
    val addComponentToRecipeUseCase = remember { AddComponentToRecipeUseCase(recipeRepository) }
    val calculateRecipeCostUseCase = remember {
        CalculateRecipeCostUseCase(
            recipeRepository = recipeRepository,
            costCalculatorService = costCalculatorService,
        )
    }

    Window(
        onCloseRequest = ::exitApplication,
        title = "Recipe Cost Calculator",
    ) {
        MaterialTheme {
            Surface(modifier = Modifier.fillMaxSize()) {
                AppContent(
                    ingredientRepository = ingredientRepository,
                    recipeRepository = recipeRepository,
                    createIngredientUseCase = createIngredientUseCase,
                    updateIngredientCostUseCase = updateIngredientCostUseCase,
                    createRecipeUseCase = createRecipeUseCase,
                    addComponentToRecipeUseCase = addComponentToRecipeUseCase,
                    calculateRecipeCostUseCase = calculateRecipeCostUseCase,
                )
            }
        }
    }
}

@Composable
private fun AppContent(
    ingredientRepository: IngredientRepository,
    recipeRepository: RecipeRepository,
    createIngredientUseCase: CreateIngredientUseCase,
    updateIngredientCostUseCase: UpdateIngredientCostUseCase,
    createRecipeUseCase: CreateRecipeUseCase,
    addComponentToRecipeUseCase: AddComponentToRecipeUseCase,
    calculateRecipeCostUseCase: CalculateRecipeCostUseCase,
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
                ingredientRepository = ingredientRepository,
                createIngredientUseCase = createIngredientUseCase,
                updateIngredientCostUseCase = updateIngredientCostUseCase,
            )

            AppScreen.RECIPES -> RecipeScreen(
                ingredientRepository = ingredientRepository,
                recipeRepository = recipeRepository,
                createRecipeUseCase = createRecipeUseCase,
                addComponentToRecipeUseCase = addComponentToRecipeUseCase,
            )

            AppScreen.COSTS -> CostScreen(
                recipeRepository = recipeRepository,
                calculateRecipeCostUseCase = calculateRecipeCostUseCase,
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
