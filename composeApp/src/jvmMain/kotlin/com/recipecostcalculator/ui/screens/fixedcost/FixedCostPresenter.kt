package com.recipecostcalculator.ui.screens.fixedcost

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.recipecostcalculator.fixedcost.application.dto.FixedCostSummaryDto
import com.recipecostcalculator.fixedcost.application.usecase.CalculateFixedCostSummaryUseCase
import com.recipecostcalculator.fixedcost.application.usecase.GetFixedCostItemsUseCase
import com.recipecostcalculator.fixedcost.application.usecase.GetProductionConfigurationUseCase
import com.recipecostcalculator.fixedcost.domain.model.FixedCostItem
import com.recipecostcalculator.ui.components.parseDecimalOrNull

class FixedCostPresenter(
    private val getFixedCostItemsUseCase: GetFixedCostItemsUseCase,
    private val getProductionConfigurationUseCase: GetProductionConfigurationUseCase,
    private val calculateFixedCostSummaryUseCase: CalculateFixedCostSummaryUseCase,
    private val addFixedCostItem: (String, java.math.BigDecimal) -> Unit,
    private val updateProductionPerMonth: (java.math.BigDecimal) -> Unit,
) {
    var items by mutableStateOf(emptyList<FixedCostItem>())
        private set

    var summary by mutableStateOf<FixedCostSummaryDto?>(null)
        private set

    var conceptNameInput by mutableStateOf("")
        private set

    var conceptAmountInput by mutableStateOf("")
        private set

    var productionInput by mutableStateOf("0")
        private set

    var message by mutableStateOf<String?>(null)
        private set

    init {
        refresh()
    }

    fun updateConceptNameInput(value: String) {
        conceptNameInput = value
    }

    fun updateConceptAmountInput(value: String) {
        conceptAmountInput = value
    }

    fun updateProductionInput(value: String) {
        productionInput = value
    }

    fun createFixedCostItem() {
        val amount = parseDecimalOrNull(conceptAmountInput)
        if (conceptNameInput.isBlank() || amount == null) {
            message = "Completa concepto y monto mensual valido"
            return
        }

        addFixedCostItem(conceptNameInput, amount)
        conceptNameInput = ""
        conceptAmountInput = ""
        message = "Gasto fijo guardado"
        refresh()
    }

    fun saveProductionPerMonth() {
        val production = parseDecimalOrNull(productionInput)
        if (production == null || production.signum() < 0) {
            message = "La produccion mensual debe ser cero o mayor"
            return
        }

        updateProductionPerMonth(production)
        message = "Produccion mensual actualizada"
        refresh()
    }

    fun refresh() {
        items = getFixedCostItemsUseCase.execute()
        val configuration = getProductionConfigurationUseCase.execute()
        productionInput = configuration.pizzasPerMonth.stripTrailingZeros().toPlainString()
        summary = calculateFixedCostSummaryUseCase.execute()
        if (items.isEmpty()) {
            message = "Carga tus gastos fijos mensuales para completar el costo total"
        }
    }
}
