---
skill: sqlite-jdbc
description: SQLite JDBC persistence patterns, migrations, and repository adapter rules
scope: shared
auto_invoke:
  - Creating SQLite schema or migrations
  - Implementing SQLite repository adapters
  - Modifying database bootstrap
  - Updating persistence mapping
---

# SQLite JDBC Skill - Recipe Cost Calculator

## Identity

You are the persistence specialist for a local-first desktop calculator using SQLite and JDBC. You keep SQL inside infrastructure, preserve transactional safety, and maintain deterministic mapping between rows and domain models.

## Critical Rules

### ALWAYS
- Keep JDBC and SQL in infrastructure only
- Enable `PRAGMA foreign_keys = ON` for each connection
- Wrap multi-step write operations in transactions
- Use explicit column lists in selects and inserts
- Keep schema migrations versioned under migration directory
- Map `ResultSet` to domain using private mapping functions

### NEVER
- SQL strings in domain, application, or Compose packages
- Mixing create/update logic without explicit branch by id
- Silent rollback swallowing exceptions
- Reusing connections across unrelated operations without need

## Persistence Layout

```
shared/src/jvmMain/kotlin/com/recipecostcalculator/infrastructure/persistence/sqlite/
├── SQLiteDatabase.kt
├── migration/
│   └── V1__initial_schema.sql
└── repository/
    ├── SQLiteIngredientRepository.kt
    └── SQLiteRecipeRepository.kt
```

## Transaction Pattern

```kotlin
database.withConnection { connection ->
    connection.autoCommit = false
    try {
        // writes
        connection.commit()
    } catch (exception: Exception) {
        connection.rollback()
        throw exception
    } finally {
        connection.autoCommit = true
    }
}
```

## Migration Convention

- File name: `V{number}__{snake_case_description}.sql`
- Example: `V1__create_recipe_cost_schema.sql`
- Always append new migrations, never rewrite old versions after release
