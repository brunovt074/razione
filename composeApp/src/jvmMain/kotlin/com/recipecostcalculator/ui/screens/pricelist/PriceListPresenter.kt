package com.recipecostcalculator.ui.screens.pricelist

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.recipecostcalculator.pricing.application.dto.PriceListRowDto
import com.recipecostcalculator.pricing.application.usecase.BuildPriceListRowsUseCase
import com.recipecostcalculator.pricing.application.usecase.CalculatePricingSummaryQuery
import com.recipecostcalculator.pricing.application.usecase.CalculatePricingSummaryUseCase
import com.recipecostcalculator.pricing.application.usecase.GetPriceListCostBasisUseCase
import com.recipecostcalculator.pricing.application.usecase.GetPriceListProductsUseCase
import com.recipecostcalculator.pricing.application.usecase.SetPriceListCostBasisCommand
import com.recipecostcalculator.pricing.application.usecase.SetPriceListCostBasisUseCase
import com.recipecostcalculator.pricing.domain.model.CostBasisType
import com.recipecostcalculator.pricing.domain.model.PriceListProduct
import com.recipecostcalculator.recipe.application.usecase.GetAllRecipesUseCase
import com.recipecostcalculator.ui.components.parseDecimalOrNull
import java.math.BigDecimal

class PriceListPresenter(
    private val getPriceListProductsUseCase: GetPriceListProductsUseCase,
    private val getPriceListCostBasisUseCase: GetPriceListCostBasisUseCase,
    private val setPriceListCostBasisUseCase: SetPriceListCostBasisUseCase,
    private val buildPriceListRowsUseCase: BuildPriceListRowsUseCase,
    private val addPriceListProduct: (String, BigDecimal, String) -> Unit,
    private val calculatePricingSummaryUseCase: CalculatePricingSummaryUseCase,
    private val getAllRecipes: GetAllRecipesUseCase,
) {
    var rows by mutableStateOf(emptyList<PriceListRowDto>())
        private set

    var costBasisType by mutableStateOf(CostBasisType.VARIABLE)
        private set

    var productNameInput by mutableStateOf("")
        private set

    var sellingPriceInput by mutableStateOf("")
        private set

    var notesInput by mutableStateOf("")
        private set

    var message by mutableStateOf<String?>(null)
        private set

    init {
        refresh()
    }

    fun updateProductNameInput(value: String) {
        productNameInput = value
    }

    fun updateSellingPriceInput(value: String) {
        sellingPriceInput = value
    }

    fun updateNotesInput(value: String) {
        notesInput = value
    }

    fun selectCostBasis(value: CostBasisType) {
        setPriceListCostBasisUseCase.execute(SetPriceListCostBasisCommand(costBasisType = value))
        costBasisType = value
        recalculateRows()
    }

    fun addProduct() {
        val sellingPrice = parseDecimalOrNull(sellingPriceInput)
        if (productNameInput.isBlank() || sellingPrice == null) {
            message = "Completa producto y precio valido"
            return
        }

        addPriceListProduct(productNameInput, sellingPrice, notesInput)
        productNameInput = ""
        sellingPriceInput = ""
        notesInput = ""
        message = "Producto agregado"
        refresh()
    }

    fun refresh() {
        val products = getPriceListProductsUseCase.execute()
        costBasisType = getPriceListCostBasisUseCase.execute().costBasisType
        recalculateRows(products)
        if (products.isEmpty()) {
            message = "Agrega productos para empezar tu lista de precios"
        }
    }

    private fun recalculateRows(products: List<PriceListProduct>? = null) {
        val recipeId = getAllRecipes.execute().firstOrNull()?.id
        if (recipeId == null) {
            rows = emptyList()
            return
        }

        val currentProducts = products ?: getPriceListProductsUseCase.execute()
        if (currentProducts.isEmpty()) {
            rows = emptyList()
            return
        }

        val pricingSummary = calculatePricingSummaryUseCase.execute(CalculatePricingSummaryQuery(recipeId = recipeId))
        rows = buildPriceListRowsUseCase.execute(
            variableCostPerPizza = pricingSummary.variableCostPerPizza,
            totalCostPerPizza = pricingSummary.totalCostPerPizza,
        )
    }
}
