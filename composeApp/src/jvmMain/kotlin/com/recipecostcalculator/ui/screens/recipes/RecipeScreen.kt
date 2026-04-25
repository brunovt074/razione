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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.recipecostcalculator.application.usecase.recipe.AddComponentToRecipeUseCase
import com.recipecostcalculator.application.usecase.recipe.CreateRecipeUseCase
import com.recipecostcalculator.domain.model.ingredient.Ingredient
import com.recipecostcalculator.domain.model.recipe.IngredientComponent
import com.recipecostcalculator.domain.model.recipe.Recipe
import com.recipecostcalculator.domain.model.recipe.SubRecipeComponent
import com.recipecostcalculator.domain.model.valueobject.Quantity
import com.recipecostcalculator.domain.model.valueobject.UnitOfMeasure
import com.recipecostcalculator.domain.repository.IngredientRepository
import com.recipecostcalculator.domain.repository.RecipeRepository
import com.recipecostcalculator.ui.components.SimpleDropdown
import com.recipecostcalculator.ui.components.parseDecimalOrNull

@Composable
fun RecipeScreen(
    ingredientRepository: IngredientRepository,
    recipeRepository: RecipeRepository,
    createRecipeUseCase: CreateRecipeUseCase,
    addComponentToRecipeUseCase: AddComponentToRecipeUseCase,
) {
    var refreshKey by remember { mutableStateOf(0) }
    val recipes = remember(refreshKey) { recipeRepository.findAll() }
    val ingredients = remember(refreshKey) { ingredientRepository.findAll() }

    var recipeNameInput by remember { mutableStateOf("") }
    var recipeYieldInput by remember { mutableStateOf("1") }

    var selectedRecipeId by remember { mutableStateOf<Long?>(null) }
    var selectedIngredientId by remember { mutableStateOf<Long?>(null) }
    var selectedSubRecipeId by remember { mutableStateOf<Long?>(null) }
    var componentQuantityInput by remember { mutableStateOf("1") }
    var componentUnit by remember { mutableStateOf(UnitOfMeasure.UNIT) }
    var message by remember { mutableStateOf<String?>(null) }

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
                    value = recipeNameInput,
                    onValueChange = { recipeNameInput = it },
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = recipeYieldInput,
                    onValueChange = { recipeYieldInput = it },
                    label = { Text("Rendimiento (unidades)") },
                    modifier = Modifier.fillMaxWidth(),
                )
                Button(
                    onClick = {
                        val yieldValue = parseDecimalOrNull(recipeYieldInput)
                        if (recipeNameInput.isBlank() || yieldValue == null) {
                            message = "Completa nombre y rendimiento valido"
                            return@Button
                        }
                        createRecipeUseCase(
                            CreateRecipeUseCase.Command(
                                name = recipeNameInput,
                                yield = Quantity.of(yieldValue, UnitOfMeasure.UNIT),
                            ),
                        )
                        recipeNameInput = ""
                        recipeYieldInput = "1"
                        message = "Receta creada"
                        refreshKey++
                    },
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
                    options = recipes,
                    selected = recipes.firstOrNull { it.id == selectedRecipeId },
                    optionLabel = { it.name },
                    onSelected = { selectedRecipeId = it.id },
                )

                SimpleDropdown(
                    label = "Ingrediente",
                    options = ingredients,
                    selected = ingredients.firstOrNull { it.id == selectedIngredientId },
                    optionLabel = { it.name },
                    onSelected = {
                        selectedIngredientId = it.id
                        selectedSubRecipeId = null
                        componentUnit = it.unit
                    },
                )

                SimpleDropdown(
                    label = "Sub-receta",
                    options = recipes,
                    selected = recipes.firstOrNull { it.id == selectedSubRecipeId },
                    optionLabel = { it.name },
                    onSelected = {
                        selectedSubRecipeId = it.id
                        selectedIngredientId = null
                        componentUnit = UnitOfMeasure.UNIT
                    },
                )

                OutlinedTextField(
                    value = componentQuantityInput,
                    onValueChange = { componentQuantityInput = it },
                    label = { Text("Cantidad") },
                    modifier = Modifier.fillMaxWidth(),
                )

                if (selectedIngredientId != null) {
                    Text("Unidad fijada por ingrediente: ${componentUnit.symbol}")
                } else if (selectedSubRecipeId != null) {
                    Text("Unidad fijada por sub-receta: ${UnitOfMeasure.UNIT.symbol}")
                } else {
                    SimpleDropdown(
                        label = "Unidad componente",
                        options = UnitOfMeasure.entries,
                        selected = componentUnit,
                        optionLabel = { "${it.name} (${it.symbol})" },
                        onSelected = { componentUnit = it },
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            val recipeId = selectedRecipeId
                            val ingredientId = selectedIngredientId
                            val quantityValue = parseDecimalOrNull(componentQuantityInput)
                            if (recipeId == null || ingredientId == null || quantityValue == null) {
                                message = "Selecciona receta, ingrediente y cantidad valida"
                                return@Button
                            }

                            addComponentToRecipeUseCase(
                                AddComponentToRecipeUseCase.Command(
                                    recipeId = recipeId,
                                    component = IngredientComponent(
                                        ingredientId = ingredientId,
                                        quantity = Quantity.of(quantityValue, componentUnit),
                                    ),
                                ),
                            )
                            message = "Ingrediente agregado"
                            refreshKey++
                        },
                    ) {
                        Text("Agregar ingrediente")
                    }

                    Button(
                        onClick = {
                            val recipeId = selectedRecipeId
                            val subRecipeId = selectedSubRecipeId
                            val quantityValue = parseDecimalOrNull(componentQuantityInput)
                            if (recipeId == null || subRecipeId == null || quantityValue == null) {
                                message = "Selecciona receta, sub-receta y cantidad valida"
                                return@Button
                            }

                            addComponentToRecipeUseCase(
                                AddComponentToRecipeUseCase.Command(
                                    recipeId = recipeId,
                                    component = SubRecipeComponent(
                                        recipeId = subRecipeId,
                                        quantity = Quantity.of(quantityValue, UnitOfMeasure.UNIT),
                                    ),
                                ),
                            )
                            message = "Sub-receta agregada"
                            refreshKey++
                        },
                    ) {
                        Text("Agregar sub-receta")
                    }
                }
            }
        }

        Text("Listado", style = MaterialTheme.typography.titleMedium)
        if (recipes.isEmpty()) {
            Text("No hay recetas cargadas")
        } else {
            recipes.forEach { recipe ->
                RecipeRow(recipe, ingredients, recipes)
            }
        }

        message?.let { Text(it) }
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
