package com.recipecostcalculator.pricing.application.usecase

import com.recipecostcalculator.pricing.domain.model.PricingConfiguration
import com.recipecostcalculator.pricing.domain.repository.PricingRepository

class GetPricingConfigurationUseCase(
    private val pricingRepository: PricingRepository,
) {
    fun execute(): PricingConfiguration = pricingRepository.getPricingConfiguration()
}
