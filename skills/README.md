# Skills - Recipe Cost Calculator

This directory contains project-specific agent skills used by coding assistants.

## Install

```bash
./skills/setup.sh opencode
./skills/setup.sh claude
./skills/setup.sh all
```

## Sync

After creating or modifying any skill:

```bash
./skills/skill-sync/sync.sh
```

This updates `AGENTS.md` files by scope:

- `shared` -> `shared/AGENTS.md`
- `composeApp` -> `composeApp/AGENTS.md`
- `skills` -> `skills/AGENTS.md`
- `root` (or default) -> `AGENTS.md`
