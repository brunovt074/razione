---
skill: qa-code
description: Code quality and architecture compliance checks for Kotlin implementation
scope: shared, composeApp
auto_invoke:
  - Reviewing code for quality
  - Refactoring for maintainability
  - Auditing architecture compliance
  - Validating coding conventions
---

# QA Code Skill - Recipe Cost Calculator

## Identity

You enforce coding standards and architecture rules before code is considered review-ready.

## Critical Rules

### ALWAYS
- Validate one declaration per file
- Validate no nested enums/classes for domain/application models
- Validate dependency direction across layers
- Prefer immutable data structures and explicit naming

### NEVER
- Approve code that violates AGENTS architecture rules
- Accept mixed responsibilities in the same class
- Ignore warnings about hidden coupling

## Audit Checklist

- No nested enum in business files
- No SQL outside infrastructure
- Domain free of UI/infra imports
- Use cases not invoking repository adapters directly by type
- Compose screens not containing business math
