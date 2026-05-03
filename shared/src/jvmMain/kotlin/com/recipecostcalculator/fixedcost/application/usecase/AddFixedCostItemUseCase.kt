package com.recipecostcalculator.fixedcost.application.usecase

import com.recipecostcalculator.fixedcost.domain.model.FixedCostItem
import com.recipecostcalculator.fixedcost.domain.repository.FixedCostRepository

class AddFixedCostItemUseCase(
    private val fixedCostRepository: FixedCostRepository,
) {
    fun execute(command: AddFixedCostItemCommand): FixedCostItem {
        val item = FixedCostItem(
            name = command.name.trim(),
            monthlyAmount = command.monthlyAmount,
        )
        return fixedCostRepository.saveItem(item)
    }
}
