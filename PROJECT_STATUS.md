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

### Step 2: Agentic Architecture Foundation [IN PROGRESS]
**Branch**: `feature/agentic-arch`

- AGENTS governance being adapted from ERP reference
- Skills tree and synchronization workflow to be finalized
- Domain-specific architecture decisions and rules to be codified

### Step 3: Starting Business Implementation [PENDING]
**Branch**: `feature/starting-implementation-business-mvp`

- Migrate previous implementation into modular architecture
- Apply skill rules strictly (architecture, sqlite-jdbc, testing, compose)
- Add compliance audit against AGENTS + skills

---

## Immediate Next Actions

1. Finalize skills in `skills/` and sync AGENTS files
2. Create branch `feature/starting-implementation-business-mvp` from `feature/agentic-arch`
3. Move business code into module-per-domain structure under `shared/`
4. Add tests and run full validation command
5. Execute architecture audit and document findings

---

## Validation Commands

```bash
./gradlew :shared:jvmTest --no-daemon
./gradlew :composeApp:compileKotlinJvm --no-daemon
./gradlew :shared:jvmTest :composeApp:compileKotlinJvm --no-daemon
```
