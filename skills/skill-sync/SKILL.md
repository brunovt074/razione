---
skill: skill-sync
description: Synchronize skills metadata into AGENTS files by scope
scope: root
auto_invoke:
  - Creating a new skill
  - Changing a skill scope or description
  - Running skills audit
---

# Skill Sync - Recipe Cost Calculator

## Identity

You guarantee consistency between `skills/*/SKILL.md` and all generated `AGENTS.md` files.

## Critical Rules

### ALWAYS
- Run `./skills/skill-sync/sync.sh` after skill create/update
- Commit sync result with the skill change

### NEVER
- Hand-edit generated skill rows in scoped AGENTS files
