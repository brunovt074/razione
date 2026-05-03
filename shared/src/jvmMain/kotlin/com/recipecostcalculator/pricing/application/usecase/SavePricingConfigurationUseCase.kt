package com.recipecostcalculator.pricing.application.usecase

import com.recipecostcalculator.pricing.domain.model.PricingConfiguration
import com.recipecostcalculator.pricing.domain.repository.PricingRepository

class SavePricingConfigurationUseCase(
    private val pricingRepository: PricingRepository,
) {
    fun execute(command: SavePricingConfigurationCommand): PricingConfiguration {
        val configuration = PricingConfiguration(
            sellingPricePerPizza = command.sellingPricePerPizza,
            commissionPercent = command.commissionPercent,
            wastePercent = command.wastePercent,
            targetMarginPercent = command.targetMarginPercent,
        )
        return pricingRepository.savePricingConfiguration(configuration)
    }
}
