package com.recipecostcalculator.pricing.application.usecase

import com.recipecostcalculator.financial.domain.model.Money
import com.recipecostcalculator.pricing.application.dto.PriceListRowDto
import com.recipecostcalculator.pricing.domain.model.CostBasisType
import com.recipecostcalculator.pricing.domain.repository.PricingRepository
import java.math.BigDecimal
import java.math.RoundingMode

class BuildPriceListRowsUseCase(
    private val pricingRepository: PricingRepository,
) {
    fun execute(variableCostPerPizza: Money, totalCostPerPizza: Money): List<PriceListRowDto> {
        val products = pricingRepository.findAllProducts()
        val basis = pricingRepository.getPriceListConfiguration().costBasisType
        val selectedCost = when (basis) {
            CostBasisType.VARIABLE -> variableCostPerPizza
            CostBasisType.TOTAL -> totalCostPerPizza
        }

        return products.map { product ->
            val grossProfit = product.sellingPrice - selectedCost
            val marginPercent = if (product.sellingPrice.amount.signum() == 0) {
                BigDecimal.ZERO
            } else {
                grossProfit.amount
                    .divide(product.sellingPrice.amount, 6, RoundingMode.HALF_UP)
                    .multiply(BigDecimal("100"))
            }

            PriceListRowDto(
                productId = product.id,
                productName = product.name,
                sellingPrice = product.sellingPrice.toDisplay(),
                costUsed = selectedCost.toDisplay(),
                grossProfit = grossProfit.toDisplay(),
                marginPercent = marginPercent.setScale(2, RoundingMode.HALF_UP).toPlainString(),
                notes = product.notes,
            )
        }
    }
}
