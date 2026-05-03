package com.recipecostcalculator.fixedcost.application.usecase

import com.recipecostcalculator.fixedcost.domain.model.ProductionConfiguration
import com.recipecostcalculator.fixedcost.domain.repository.FixedCostRepository

class GetProductionConfigurationUseCase(
    private val fixedCostRepository: FixedCostRepository,
) {
    fun execute(): ProductionConfiguration = fixedCostRepository.getProductionConfiguration()
}
