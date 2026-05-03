package com.recipecostcalculator.fixedcost.domain.repository

import com.recipecostcalculator.fixedcost.domain.model.FixedCostItem
import com.recipecostcalculator.fixedcost.domain.model.ProductionConfiguration

interface FixedCostRepository {
    fun saveItem(item: FixedCostItem): FixedCostItem
    fun findAllItems(): List<FixedCostItem>
    fun saveProductionConfiguration(configuration: ProductionConfiguration): ProductionConfiguration
    fun getProductionConfiguration(): ProductionConfiguration
}
