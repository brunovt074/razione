package com.recipecostcalculator.fixedcost

import com.recipecostcalculator.fixedcost.domain.model.FixedCostItem
import com.recipecostcalculator.fixedcost.domain.model.ProductionConfiguration
import com.recipecostcalculator.fixedcost.domain.repository.FixedCostRepository
import java.math.BigDecimal

class FakeFixedCostRepository : FixedCostRepository {
    private val store = mutableMapOf<Long, FixedCostItem>()
    private var nextId = 1L
    private var productionConfiguration = ProductionConfiguration(pizzasPerMonth = BigDecimal.ZERO)

    override fun saveItem(item: FixedCostItem): FixedCostItem {
        val persisted = if (item.id == 0L) {
            item.copy(id = nextId++)
        } else {
            item
        }
        store[persisted.id] = persisted
        return persisted
    }

    override fun findAllItems(): List<FixedCostItem> = store.values.sortedBy { it.name }

    override fun saveProductionConfiguration(configuration: ProductionConfiguration): ProductionConfiguration {
        productionConfiguration = configuration
        return productionConfiguration
    }

    override fun getProductionConfiguration(): ProductionConfiguration = productionConfiguration
}
