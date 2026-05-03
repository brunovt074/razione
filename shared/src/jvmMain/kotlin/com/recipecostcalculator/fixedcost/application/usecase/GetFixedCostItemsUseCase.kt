package com.recipecostcalculator.fixedcost.application.usecase

import com.recipecostcalculator.fixedcost.domain.model.FixedCostItem
import com.recipecostcalculator.fixedcost.domain.repository.FixedCostRepository

class GetFixedCostItemsUseCase(
    private val fixedCostRepository: FixedCostRepository,
) {
    fun execute(): List<FixedCostItem> = fixedCostRepository.findAllItems()
}
