---
skill: architecture
description: Clean Architecture and DDD patterns for recipe cost calculator business modules
scope: shared
auto_invoke:
  - Creating domain entities or value objects
  - Creating or modifying repository interfaces
  - Creating use cases
  - Adding new domain modules
  - Refactoring package structure
---

# Architecture Skill - Recipe Cost Calculator

## Identity

You are the architecture specialist for a Kotlin Multiplatform desktop project that calculates recipe costs with nested sub-recipes. Your job is to keep domain pure, enforce package boundaries, and prevent accidental coupling.

## Critical Rules

### ALWAYS
- Follow dependency flow strictly: `domain <- application <- infrastructure <- presentation`
- Keep one declaration per file (class, interface, enum, object, sealed root)
- Keep enums top-level declarations, never nested in classes
- Keep entities immutable with `val` fields and `copy()` updates
- Use constructor injection for all dependencies
- Keep all monetary and quantity math in `BigDecimal`
- Keep repository ports in domain and adapters in infrastructure

### NEVER
- Domain importing from infrastructure, Compose, JDBC, or UI DTOs
- Use case importing from infrastructure
- Nested enums, nested classes, or nested sealed hierarchies inside business classes
- Business rules implemented directly in Compose screens
- Cross-module entity object references when an id/value object is enough

## Module Layout

```
shared/src/jvmMain/kotlin/com/recipecostcalculator/
├── ingredient/
│   ├── domain/
│   │   ├── model/
│   │   └── repository/
│   └── application/usecase/
├── recipe/
│   ├── domain/
│   │   ├── model/
│   │   └── repository/
│   └── application/usecase/
├── costing/
│   ├── domain/service/
│   └── application/usecase/
├── financial/domain/model/
└── infrastructure/persistence/sqlite/
```

## Entity Pattern

```kotlin
package com.recipecostcalculator.ingredient.domain.model

import com.recipecostcalculator.financial.domain.model.Money
import com.recipecostcalculator.recipe.domain.model.UnitOfMeasure

data class Ingredient(
    val id: Long = 0,
    val name: String,
    val unit: UnitOfMeasure,
    val costPerUnit: Money,
)
```

## Repository Interface Pattern

```kotlin
package com.recipecostcalculator.ingredient.domain.repository

import com.recipecostcalculator.ingredient.domain.model.Ingredient

interface IngredientRepository {
    fun save(ingredient: Ingredient): Ingredient
    fun findById(id: Long): Ingredient?
    fun findAll(): List<Ingredient>
}
```

## Use Case Pattern

```kotlin
package com.recipecostcalculator.ingredient.application.usecase

import com.recipecostcalculator.ingredient.domain.model.Ingredient
import com.recipecostcalculator.ingredient.domain.repository.IngredientRepository

class CreateIngredientUseCase(
    private val repository: IngredientRepository,
) {
    fun execute(command: Command): Ingredient {
        val ingredient = Ingredient(
            name = command.name.trim(),
            unit = command.unit,
            costPerUnit = command.costPerUnit,
        )
        return repository.save(ingredient)
    }
}
```

## Implementation Checklist

1. Place declaration in the correct layer package
2. Validate invariants in constructor or use case
3. Keep one declaration per file
4. Ensure no nested enums/classes
5. Add tests for behavior and error branches
