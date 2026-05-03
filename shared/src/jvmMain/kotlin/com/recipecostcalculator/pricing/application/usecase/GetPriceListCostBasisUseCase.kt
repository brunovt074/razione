package com.recipecostcalculator.pricing.application.usecase

import com.recipecostcalculator.pricing.domain.model.PriceListConfiguration
import com.recipecostcalculator.pricing.domain.repository.PricingRepository

class GetPriceListCostBasisUseCase(
    private val pricingRepository: PricingRepository,
) {
    fun execute(): PriceListConfiguration = pricingRepository.getPriceListConfiguration()
}
