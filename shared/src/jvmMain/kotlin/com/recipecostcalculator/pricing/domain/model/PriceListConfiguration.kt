package com.recipecostcalculator.pricing.domain.model

data class PriceListConfiguration(
    val costBasisType: CostBasisType,
) {
    companion object {
        fun defaults(): PriceListConfiguration = PriceListConfiguration(costBasisType = CostBasisType.VARIABLE)
    }
}
