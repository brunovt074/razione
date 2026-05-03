package com.recipecostcalculator.pricing.application.usecase

import com.recipecostcalculator.pricing.domain.model.PriceListProduct
import com.recipecostcalculator.pricing.domain.repository.PricingRepository

class GetPriceListProductsUseCase(
    private val pricingRepository: PricingRepository,
) {
    fun execute(): List<PriceListProduct> = pricingRepository.findAllProducts()
}
