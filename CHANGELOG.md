# Changelog

All notable changes to this project will be documented in this file.

The format is based on Keep a Changelog,
and this project follows Semantic Versioning.

## [Unreleased]

### Added

- Base Compose Multiplatform Gradle setup (`feature/initial-config`)
- Agent governance baseline with project-specific `AGENTS.md`
- Project management files: `PROJECT_STATUS.md`, `CHANGELOG.md`
- Full skill system with scoped AGENTS sync (`skills/setup.sh`, `skills/skill-sync/sync.sh`)
- Domain modularization in `shared`: `ingredient`, `recipe`, `costing`, `financial`, `infrastructure`
- Presenter-driven UI state management for ingredient, recipe, and costing screens
- Use-case commands/queries split into dedicated files (one declaration per file)
- Initial unit tests for use cases and recursive costing flow
- SQL migration file `shared/src/jvmMain/resources/db/migration/V1__create_recipe_cost_schema.sql`

### Changed

- SQLite infrastructure moved from `composeApp` to `shared` infrastructure layer
- Domain ids migrated from nullable (`Long?`) to explicit persisted semantic (`Long`, `0` unsaved)
- Navigation enum extracted from `Main.kt` to top-level `AppScreen.kt`

### Verified

- Compliance audit against AGENTS and skills rules executed
- Validation command passed: `./gradlew :shared:jvmTest :composeApp:compileKotlinJvm --no-daemon`
