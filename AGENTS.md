# Recipe Cost Calculator Desktop - Agent Guidelines

> Single Source of Truth for all AI coding agents working on this project.
> Compatible with: OpenCode, Claude Code, Codex, Cursor, Windsurf, Gemini CLI, Copilot, Aider, Junie.

---

## Agent Startup Protocol

Before writing any code, every agent MUST:

1. Read this file completely
2. Read `PROJECT_STATUS.md` to understand current state and branch chain
3. Identify which skill(s) apply to the task (see Auto-invoke table below)
4. Read the relevant `skills/{name}/SKILL.md` file(s)
5. Confirm the target branch before any file modification

Skipping this protocol produces inconsistent code and failed reviews.

---

## Project Overview

| Field | Value |
|-------|-------|
| Name | Recipe Cost Calculator |
| Language | Kotlin 2.2.21 |
| Platform | Kotlin Multiplatform (Android-first, desktop planned) |
| Architecture | Clean Architecture / DDD / MVI |
| Database | SQLDelight (SQLite nativo por plataforma) |
| UI | Jetpack Compose 1.9.1 + Voyager (navegación) |
| DI | Koin 4.2.0 (KMP) |
| Navigation | Voyager 1.1.0-beta02 |
| Money | java.math.BigDecimal |
| Date/Time | kotlinx-datetime |

### Component Layout

| Component | Location | Purpose |
|-----------|----------|---------|
| Shared | `shared/` | Domain, application, infrastructure core |
| Desktop App | `composeApp/` | Compose Multiplatform desktop UI |
| Skills | `skills/` | Agent behavior, coding patterns, quality gates |
| Build Config | `gradle/libs.versions.toml` | Version catalog |

---

## Setup & Validation Commands

```bash
# Unit tests for shared module
./gradlew :shared:jvmTest --no-daemon

# Compile desktop app
./gradlew :composeApp:compileKotlinJvm --no-daemon

# Run desktop app
./gradlew :composeApp:run --no-daemon

# Full pre-merge validation
./gradlew :shared:jvmTest :composeApp:compileKotlinJvm --no-daemon
```

## Agent Skills Setup

Install skills for your AI coding agent of choice:

```bash
./skills/setup.sh
./skills/setup.sh opencode
./skills/setup.sh claude
./skills/setup.sh gemini
./skills/setup.sh codex
./skills/setup.sh copilot
./skills/setup.sh all
```

After creating or modifying any skill, sync to all `AGENTS.md` files:

```bash
./skills/skill-sync/sync.sh
```

---

## Code Style

- No comments in production code - naming and structure must explain intent
- No SQL outside infrastructure packages
- One declaration per file for classes, interfaces, enums, objects, and sealed roots
- Enums must be top-level declarations, never nested inside classes
- BigDecimal for all monetary calculations, no Float/Double for money
- Domain layer NEVER imports infrastructure or UI
- UI text in Spanish; code, package names, and tests in English
- No wildcard imports except when explicitly allowed in a skill

---

## Testing Rules

- 100% coverage target for use cases and repositories
- No garbage tests (`assertTrue(true)`, no-op assertions, empty tests)
- Every test must represent a real business scenario
- Fake repositories for unit tests
- SQLite test database for infrastructure integration tests
- Use `*TestFactory` objects for deterministic test data creation
- Tests must be fully independent (no cross-test mutable shared state)

---

## Git & Branch Strategy

- NEVER commit directly to `master`
- Use feature branches with descriptive names: `feature/descriptive-name`
- Atomic conventional commits: `feat:`, `fix:`, `test:`, `refactor:`, `chore:`
- Validate branch chain before implementation

Mandatory chain for this project baseline:

```
master
  <- feature/initial-config
      <- feature/agentic-arch
          <- feature/starting-implementation-business-mvp

Mobile-first chain (new paradigm):

master
  <- feature/agentic-arch
      <- feature/mobile-first-architecture
```

Branch chaining is allowed and preferred over direct merge to `master`.

---

## Architecture Rules

```
shared/src/
├── commonMain/kotlin/com/recipecostcalculator/
│   ├── domain/
│   │   ├── model/
│   │   ├── repository/
│   │   └── usecase/
│   ├── data/
│   │   ├── local/
│   │   │   ├── db/
│   │   │   └── repository/
│   │   └── remote/              (placeholder vacío — v4 API)
│   ├── presentation/
│   │   ├── viewmodel/
│   │   └── util/
│   └── di/
│       └── AppModule.kt
├── commonMain/sqldelight/        (DDL SQLDelight)
├── androidMain/kotlin/
│   └── data/local/db/
│       └── DriverFactory.android.kt
├── jvmMain/kotlin/               (stub desktop)
└── commonTest/kotlin/
```

### Mobile-First Source Sets

```
composeApp/src/
├── commonMain/kotlin/           (UI compartida)
├── androidMain/kotlin/
│   └── MainActivity.kt
├── jvmMain/kotlin/               (desktop)
└── iosMain/kotlin/               (preparado para futuro)
```

### Dependency Rule

```
domain <- application <- infrastructure <- presentation
  ^
  +-- NEVER depends on infrastructure or UI
```

- Domain: entities, value objects, repository interfaces, domain services
- Application: use cases orchestrating domain behavior
- Infrastructure: SQLite persistence, migrations, repository implementations
- Presentation: Compose UI, ViewModels, navigation, i18n

---

## Skills

Skills provide specialized context for each concern area and are mandatory.

### Available Skills

| Skill | File | Scope |
|-------|------|-------|
| `architecture` | `skills/architecture/SKILL.md` | shared |
| `sqlite-jdbc` | `skills/sqlite-jdbc/SKILL.md` | shared |
| `testing` | `skills/testing/SKILL.md` | shared, composeApp |
| `compose-frontend` | `skills/compose-frontend/SKILL.md` | composeApp |
| `ux-ui-design` | `skills/ux-ui-design/SKILL.md` | composeApp |
| `qa-code` | `skills/qa-code/SKILL.md` | shared, composeApp |
| `qa-ux-ui` | `skills/qa-ux-ui/SKILL.md` | composeApp |
| `skill-creator` | `skills/skill-creator/SKILL.md` | skills |
| `skill-sync` | `skills/skill-sync/SKILL.md` | root |
| `mobile-compose` | `skills/mobile-compose/SKILL.md` | composeApp (mobile) | <!-- skill-sync:skill-sync -->

### Auto-invoke Rules

When performing any of these actions, read the skill FIRST:

| Action | Read This Skill |
|--------|------------------|
| Creating domain entities or value objects | `architecture` |
| Creating or modifying repository interfaces | `architecture` |
| Creating use cases | `architecture` |
| Adding a new domain module | `architecture` |
| Creating SQLite schema or migrations | `sqlite-jdbc` |
| Implementing SQLite repository adapters | `sqlite-jdbc` |
| Modifying SQLite database bootstrap | `sqlite-jdbc` |
| Writing unit tests for use cases | `testing` |
| Writing integration tests for repositories | `testing` |
| Creating fake repositories or test factories | `testing` |
| Creating Compose screens or components | `compose-frontend` |
| Creating or modifying ViewModels | `compose-frontend` |
| Kotlin/Android mobile development with Jetpack Compose | `mobile-compose` |
| Mobile-first architecture, adaptive layouts, WindowSizeClass | `mobile-compose` |
| Designing user flows or UI copy in Spanish | `ux-ui-design` |
| Running code quality audits or refactors | `qa-code` |
| Creating a new skill | `skill-creator` then `skill-sync` |
| After modifying any skill metadata | `skill-sync` |

---

## Decision Log

Architectural decisions made - do not relitigate without strong evidence:

| Decision | Rationale |
|----------|-----------|
| BigDecimal for money | Prevent rounding errors in cost calculations |
| Recursive costing with cycle guard | Recipes can include sub-recipes safely |
| SQLite local-first persistence | MVP works offline without external services |
| Branch chaining strategy | Keep each stage auditable and independently reviewable |
| One declaration per file | Improves readability and agent consistency |
| No nested enums/classes | Avoid hidden coupling and bloated files |

---

## Context Files

Read these files when starting or resuming work:

| File | When to Read |
|------|--------------|
| `AGENTS.md` | Always |
| `PROJECT_STATUS.md` | Always |
| `CHANGELOG.md` | Before merge or release |
| `skills/{name}/SKILL.md` | Before touching that concern area |

---

## Current Status

See `PROJECT_STATUS.md` for implementation progress.
See `CHANGELOG.md` for release history.
