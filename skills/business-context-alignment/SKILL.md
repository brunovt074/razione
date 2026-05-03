---
skill: business-context-alignment
description: Align domain, UX/UI, IA, and workflow with the workbook business source of truth
scope: root, shared, composeApp
auto_invoke:
  - Modifying business concepts from workbook
  - Designing dashboard or navigation IA
  - Defining mobile-first flows inspired by workbook sections
  - Implementing pricing, margin, fixed-cost, or dashboard rules
  - Importing workbook or mirrored CSV business data
---

# Business Context Alignment Skill - Recipe Cost Calculator

## Identity

You preserve and apply the company business logic defined in `business-context/Planilla_Pizzas_Congeladas_Costos_y_Margen.xlsx` and mirrored `business-context/*.csv` sheets.

## Critical Rules

### ALWAYS
- Treat workbook + mirrored CSV sheets as the absolute business source of truth
- Map UI labels, IA sections, and flows to current business nomenclature before inventing new terms
- Keep mobile-first priority for UX/UI and IA decisions
- Keep dashboard as default entry and guarantee immediate access to:
  - `Costos por pizza`
  - `Nueva receta`
- Validate every proposed business rule against workbook formulas and section semantics

### NEVER
- Interpret `IA` as artificial intelligence; in this project it means `arquitectura de informacion`
- Replace business vocabulary with technical vocabulary in primary user flows
- Change formulas, section grouping, or business workflow without exhaustive analysis and stakeholder consultation
- Assume desktop-first interaction patterns when defining new UX/UI decisions

## Workbook-to-Product Mapping Baseline

- `Costos (por pizza)` -> operational costing core screen and controls
- `Gastos fijos` -> monthly fixed-cost allocation flow
- `Lista de precios` -> product pricing and margin list
- `Dashboard` -> default entry point with KPI summary and primary shortcuts

## Mobile-First IA Baseline

- Dashboard is the first screen on app start
- Dashboard shows business KPI summary and immediate actions
- `Costos por pizza` is reachable in one tap from dashboard
- `Nueva receta` is reachable in one tap from dashboard
- Desktop remains supported, but mobile-first hierarchy and wording are the reference model
