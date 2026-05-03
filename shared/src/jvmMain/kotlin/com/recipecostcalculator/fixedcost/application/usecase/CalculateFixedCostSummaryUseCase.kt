package com.recipecostcalculator.fixedcost.application.usecase

import com.recipecostcalculator.financial.domain.model.Money
import com.recipecostcalculator.fixedcost.application.dto.FixedCostSummaryDto
import com.recipecostcalculator.fixedcost.domain.repository.FixedCostRepository
import java.math.RoundingMode

class CalculateFixedCostSummaryUseCase(
    private val fixedCostRepository: FixedCostRepository,
) {
    fun execute(): FixedCostSummaryDto {
        val items = fixedCostRepository.findAllItems()
        val configuration = fixedCostRepository.getProductionConfiguration()
        val totalMonthly = items.fold(Money.zero()) { acc, item -> acc + item.monthlyAmount }
        val fixedCostPerPizza = if (configuration.pizzasPerMonth.signum() == 0) {
            Money.zero(totalMonthly.currency)
        } else {
            Money.of(
                totalMonthly.amount.divide(configuration.pizzasPerMonth, 6, RoundingMode.HALF_UP),
                totalMonthly.currency,
            )
        }

        return FixedCostSummaryDto(
            totalMonthlyFixedCost = totalMonthly,
            productionPerMonth = configuration.pizzasPerMonth,
            fixedCostPerPizza = fixedCostPerPizza,
        )
    }
}
