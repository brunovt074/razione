package com.recipecostcalculator.pricing.application.usecase

import com.recipecostcalculator.pricing.domain.model.PriceListConfiguration
import com.recipecostcalculator.pricing.domain.repository.PricingRepository

class SetPriceListCostBasisUseCase(
    private val pricingRepository: PricingRepository,
) {
    fun execute(command: SetPriceListCostBasisCommand): PriceListConfiguration {
        val configuration = PriceListConfiguration(costBasisType = command.costBasisType)
        return pricingRepository.savePriceListConfiguration(configuration)
    }
}
