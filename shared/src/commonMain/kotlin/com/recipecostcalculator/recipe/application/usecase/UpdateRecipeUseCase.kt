package com.recipecostcalculator.recipe.application.usecase

import com.recipecostcalculator.recipe.domain.model.Recipe
import com.recipecostcalculator.recipe.domain.model.RecipeIngredient
import com.recipecostcalculator.recipe.domain.repository.RecipeRepository
import com.recipecostcalculator.util.currentTimeMillis

class UpdateRecipeUseCase(
    private val repository: RecipeRepository,
) {
    data class Command(
        val id: Long,
        val name: String,
        val parentRecipeId: Long?,
        val ingredients: List<RecipeIngredient>,
    )

    suspend fun execute(command: Command): Result<Recipe> = runCatching {
        require(command.name.isNotBlank()) { "El nombre no puede estar vacío" }

        command.parentRecipeId?.let { newParentId ->
            val ancestorChain = repository.findAncestorChain(newParentId)
            val wouldCreateCycle = ancestorChain.any { it.id == command.id }
            require(!wouldCreateCycle) {
                "No se puede asignar este padre: crearía un ciclo de herencia"
            }
        }

        val existing = repository.findById(command.id)
            ?: error("Receta ${command.id} no encontrada")

        val updated = existing.copy(
            name = command.name.trim(),
            parentRecipeId = command.parentRecipeId,
            ingredients = command.ingredients,
            updatedAt = currentTimeMillis(),
        )
        repository.save(updated)
    }
}