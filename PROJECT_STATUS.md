# Recipe Cost Calculator - Project Status

## Vision

Mobile-first Kotlin Multiplatform app for recipe cost calculation with Android as primary target, iOS and Desktop future-ready.

---

## Technology Stack

| Component | Version |
|-----------|---------|
| Kotlin | 2.2.21 |
| Compose Multiplatform | 1.9.1 |
| SQLDelight | 2.1.0 |
| Koin | 4.2.0 |
| Voyager | 1.1.0-beta02 |
| Lifecycle | 2.10.0 |
| AGP | 8.7.3 |
| compileSdk | 36 |
| minSdk | 26 |
| targetSdk | 35 |

---

## Development Rules

- No comments in production code
- One declaration per file (classes, interfaces, objects, enums, sealed roots)
- No nested enums inside classes
- Domain pure from UI/infrastructure dependencies
- BigDecimal for money and quantity math
- Spanish UI text, English code and tests
- Feature branch workflow only

---

## Branch Chain Strategy

Current chain:

```
master
  <- feature/agentic-arch
      <- feature/mobile-first-architecture (current)
```

### Branch Intent

- `master`: minimal baseline commit
- `feature/agentic-arch`: AGENTS + skills + governance layer
- `feature/mobile-first-architecture`: KMP mobile-first foundation with Android target

---

## Current State

### Mobile-First Foundation [COMPLETE]
**Branch**: `feature/mobile-first-architecture`

**Completed**:
- Domain layer: 7 entities (Ingredient, Recipe, RecipeIngredient, AdditionalVariableCost, FixedCost, AppSettings, CostBreakdown)
- 5 repository interfaces with implementations
- CalculateRecipeCostUseCase with recursive recipe costing
- SQLDelight schema with 6 tables
- 4 ViewModels (Ingredientes, Recetas, CostosFijos, Dashboard)
- DatabaseSeeder with initial data (6 ingredients, Muzzarela recipe, default settings)
- Koin DI setup with Android driver
- Basic App.kt with Material3 theme

### In Progress
- Navigation with Voyager TabNavigator
- UI screens (Dashboard, Ingredientes, Recetas, CostosFijos)

---

## Validation Commands

```bash
# Run Android unit tests
./gradlew :shared:testDebugUnitTest --no-daemon

# Compile Android app
./gradlew :composeApp:compileDebugKotlin --no-daemon

# Full validation
./gradlew :shared:testDebugUnitTest :composeApp:compileDebugKotlin --no-daemon
```

---

## Next Actions

1. Create AppNavigation with Voyager TabNavigator
2. Implement DashboardScreen with KPIs
3. Implement IngredientesScreen
4. Implement RecetasScreen with inheritance
5. Implement CostosFijosScreen
6. Write unit tests for use cases and repositories