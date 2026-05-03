package com.recipecostcalculator.fixedcost.application.usecase

import com.recipecostcalculator.fixedcost.domain.model.ProductionConfiguration
import com.recipecostcalculator.fixedcost.domain.repository.FixedCostRepository

class SetProductionPerMonthUseCase(
    private val fixedCostRepository: FixedCostRepository,
) {
    fun execute(command: SetProductionPerMonthCommand): ProductionConfiguration {
        val configuration = ProductionConfiguration(
            pizzasPerMonth = command.pizzasPerMonth,
        )
        return fixedCostRepository.saveProductionConfiguration(configuration)
    }
}
