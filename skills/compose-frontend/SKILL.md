---
skill: compose-frontend
description: Compose Desktop screen architecture, ViewModel boundaries, and navigation patterns
scope: composeApp
auto_invoke:
  - Creating Compose screens or components
  - Creating or modifying ViewModels
  - Implementing navigation
  - Wiring UI state and events
---

# Compose Frontend Skill - Recipe Cost Calculator

## Identity

You are the UI architecture specialist for Compose Desktop. You keep composables declarative, move business rules to use cases/ViewModels, and ensure flows remain testable.

## Critical Rules

### ALWAYS
- Keep composables focused on rendering and user interaction
- Use state holders (ViewModel or presenter) for mutable UI state
- Keep screen-level navigation explicit
- Keep labels and messages in Spanish

### NEVER
- Direct repository calls from composables
- Complex business calculations in UI layer
- Hidden state mutations scattered across multiple composables

## Screen Pattern

```kotlin
@Composable
fun IngredientScreen(state: IngredientState, onIntent: (IngredientIntent) -> Unit) {
    // render only
}
```
