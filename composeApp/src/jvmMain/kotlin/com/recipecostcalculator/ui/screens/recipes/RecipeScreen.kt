package com.recipecostcalculator.ui.screens.recipes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.recipecostcalculator.ingredient.domain.model.Ingredient
import com.recipecostcalculator.recipe.domain.model.IngredientComponent
import com.recipecostcalculator.recipe.domain.model.Recipe
import com.recipecostcalculator.recipe.domain.model.SubRecipeComponent
import com.recipecostcalculator.recipe.domain.model.UnitOfMeasure
import com.recipecostcalculator.ui.components.SimpleDropdown

@Composable
fun RecipeScreen(
    presenter: RecipePresenter,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Recetas", style = MaterialTheme.typography.headlineSmall)

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text("Crear receta", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = presenter.recipeNameInput,
                    onValueChange = presenter::updateRecipeNameInput,
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = presenter.recipeYieldInput,
                    onValueChange = presenter::updateRecipeYieldInput,
                    label = { Text("Rendimiento (unidades)") },
                    modifier = Modifier.fillMaxWidth(),
                )
                Button(
                    onClick = presenter::createRecipe,
                ) {
                    Text("Guardar")
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text("Agregar componente", style = MaterialTheme.typography.titleMedium)

                SimpleDropdown(
                    label = "Receta destino",
                    options = presenter.recipes,
                    selected = presenter.currentSelectedRecipe(),
                    optionLabel = { it.name },
                    onSelected = presenter::selectRecipe,
                )

                SimpleDropdown(
                    label = "Ingrediente",
                    options = presenter.ingredients,
                    selected = presenter.currentSelectedIngredient(),
                    optionLabel = { it.name },
                    onSelected = presenter::selectIngredient,
                )

                SimpleDropdown(
                    label = "Sub-receta",
                    options = presenter.recipes,
                    selected = presenter.currentSelectedSubRecipe(),
                    optionLabel = { it.name },
                    onSelected = presenter::selectSubRecipe,
                )

                OutlinedTextField(
                    value = presenter.componentQuantityInput,
                    onValueChange = presenter::updateComponentQuantityInput,
                    label = { Text("Cantidad") },
                    modifier = Modifier.fillMaxWidth(),
                )

                if (presenter.selectedIngredientId != null) {
                    Text("Unidad fijada por ingrediente: ${presenter.componentUnit.symbol}")
                } else if (presenter.selectedSubRecipeId != null) {
                    Text("Unidad fijada por sub-receta: ${UnitOfMeasure.UNIT.symbol}")
                } else {
                    SimpleDropdown(
                        label = "Unidad componente",
                        options = UnitOfMeasure.entries,
                        selected = presenter.componentUnit,
                        optionLabel = { "${it.name} (${it.symbol})" },
                        onSelected = presenter::updateComponentUnit,
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = presenter::addIngredientComponent,
                    ) {
                        Text("Agregar ingrediente")
                    }

                    Button(
                        onClick = presenter::addSubRecipeComponent,
                    ) {
                        Text("Agregar sub-receta")
                    }
                }
            }
        }

        Text("Listado", style = MaterialTheme.typography.titleMedium)
        if (presenter.recipes.isEmpty()) {
            Text("No hay recetas cargadas")
        } else {
            presenter.recipes.forEach { recipe ->
                RecipeRow(recipe, presenter.ingredients, presenter.recipes)
            }
        }

        presenter.message?.let { Text(it) }
    }
}

@Composable
private fun RecipeRow(
    recipe: Recipe,
    ingredients: List<Ingredient>,
    recipes: List<Recipe>,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text("${recipe.name} (rinde ${recipe.yield.toDisplay()})")
            if (recipe.components.isEmpty()) {
                Text("Sin componentes")
            } else {
                recipe.components.forEach { component ->
                    when (component) {
                        is IngredientComponent -> {
                            val name = ingredients.firstOrNull { it.id == component.ingredientId }?.name ?: "Ingrediente #${component.ingredientId}"
                            Text("- $name: ${component.quantity.toDisplay()}")
                        }

                        is SubRecipeComponent -> {
                            val name = recipes.firstOrNull { it.id == component.recipeId }?.name ?: "Receta #${component.recipeId}"
                            Text("- Sub-receta $name: ${component.quantity.toDisplay()}")
                        }
                    }
                }
            }
        }
    }
}
