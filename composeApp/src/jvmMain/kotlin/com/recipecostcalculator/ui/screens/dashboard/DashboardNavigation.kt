package com.recipecostcalculator.ui.screens.dashboard

import com.recipecostcalculator.AppScreen

fun toAppScreen(action: DashboardAction): AppScreen {
    return when (action) {
        DashboardAction.GO_TO_COSTOS_POR_PIZZA -> AppScreen.COSTOS_POR_PIZZA
        DashboardAction.GO_TO_NUEVA_RECETA -> AppScreen.NUEVA_RECETA
        DashboardAction.GO_TO_GASTOS_FIJOS -> AppScreen.GASTOS_FIJOS
        DashboardAction.GO_TO_LISTA_PRECIOS -> AppScreen.LISTA_PRECIOS
    }
}
