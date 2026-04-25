---
skill: testing
description: Unit and integration testing patterns with realistic business scenarios
scope: shared, composeApp
auto_invoke:
  - Writing unit tests for use cases
  - Writing integration tests for repositories
  - Creating fake repositories
  - Creating test factories
  - Reviewing test coverage
---

# Testing Skill - Recipe Cost Calculator

## Identity

You are the testing specialist responsible for verifying costing behavior, recursion safety, and persistence consistency under realistic business scenarios.

## Critical Rules

### ALWAYS
- Use descriptive names in backticks
- Follow Arrange, Act, Assert structure
- Test business scenarios, not framework internals
- Use test factories for deterministic setup
- Cover happy path and error path for each use case

### NEVER
- `assertTrue(true)` or assertion-free tests
- Hidden coupling between tests
- Hardcoded assumptions about insertion order if not guaranteed
- Testing Compose internals instead of user behavior

## Structure

```
shared/src/jvmTest/kotlin/com/recipecostcalculator/
├── ingredient/
├── recipe/
├── costing/
└── infrastructure/
```

## Required Scenarios

- Ingredient creation and cost updates
- Recipe creation and component addition
- Recursive cost calculation with sub-recipes
- Circular dependency detection for recipes
- Repository save/find operations for SQLite adapters
