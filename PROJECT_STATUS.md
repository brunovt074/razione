# Recipe Cost Calculator Desktop - Project Status

## Vision

Desktop application to calculate recipe costs with composable sub-recipes, full ingredient breakdown, and local SQLite persistence.

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

Current mandatory chain:

```
master
  <- feature/initial-config
      <- feature/agentic-arch
          <- feature/starting-implementation-business-mvp
```

### Branch Intent

- `master`: minimal baseline commit
- `feature/initial-config`: clean Compose/Gradle bootstrap
- `feature/agentic-arch`: AGENTS + skills + governance layer
- `feature/starting-implementation-business-mvp`: business implementation aligned to the new agentic architecture

---

## Current State

### Step 0: Repository Baseline [DONE]
**Branch**: `master`

- Empty baseline commit created

### Step 1: Initial Compose/Gradle Configuration [DONE]
**Branch**: `feature/initial-config`

- Root Gradle settings and version catalog configured
- Gradle wrapper added
- `composeApp` and `shared` modules bootstrapped
- Compile validation for desktop target completed

### Step 2: Agentic Architecture Foundation [DONE]
**Branch**: `feature/agentic-arch`

- AGENTS governance fully adapted from ERP reference to this business domain
- Skills tree implemented with scoped synchronization (`shared`, `composeApp`, `skills`, `root`)
- Domain-specific architecture decisions codified (one declaration per file, no nested enums)

### Step 3: Starting Business Implementation [DONE]
**Branch**: `feature/starting-implementation-business-mvp`

- Business implementation migrated to module-per-domain architecture in `shared`
- Infrastructure SQLite moved from `composeApp` to `shared` and wired as adapter
- UI screens moved to presenter pattern (state and actions outside composables)
- Use cases split with explicit commands/queries in separate files
- Tests added for ingredient, recipe, and costing use cases
- Compliance audit executed against AGENTS and skills rules

---

## Immediate Next Actions

1. Review commit history and normalize non-descriptive message inherited from legacy (`autocommit`) in future cleanup branch if desired
2. Expand integration tests for SQLite repositories (currently unit-focused coverage)
3. Add migration runner strategy beyond single V1 bootstrap if schema evolves

---

## Validation Commands

```bash
./gradlew :shared:jvmTest --no-daemon
./gradlew :composeApp:compileKotlinJvm --no-daemon
./gradlew :shared:jvmTest :composeApp:compileKotlinJvm --no-daemon
```
