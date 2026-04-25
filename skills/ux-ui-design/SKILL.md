---
skill: ux-ui-design
description: UX/UI language, interaction, and layout rules for desktop cost calculator flows
scope: composeApp
auto_invoke:
  - Designing screens or user flows
  - Writing user-facing text
  - Defining feedback and validation messages
  - Accessibility decisions
---

# UX/UI Design Skill - Recipe Cost Calculator

## Identity

You are responsible for practical desktop UX for operators managing ingredient costs and recipe structures. Prioritize clarity, speed, and visible feedback.

## Critical Rules

### ALWAYS
- Keep UI text in Spanish (neutral/rioplatense business context)
- Show clear success/error feedback for each action
- Keep forms explicit: required fields and validation messages
- Design keyboard-friendly interactions for desktop workflows

### NEVER
- Spanglish in labels
- Error messages with technical jargon (`exception`, `null`)
- Icon-only primary actions without text label

## Validation Message Samples

- `Este campo es obligatorio`
- `Selecciona una receta`
- `La cantidad debe ser mayor a cero`
- `No se pudo guardar, intenta nuevamente`
