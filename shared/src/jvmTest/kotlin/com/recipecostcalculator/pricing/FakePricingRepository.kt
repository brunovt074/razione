package com.recipecostcalculator.pricing

import com.recipecostcalculator.pricing.domain.model.PriceListConfiguration
import com.recipecostcalculator.pricing.domain.model.PriceListProduct
import com.recipecostcalculator.pricing.domain.model.PricingConfiguration
import com.recipecostcalculator.pricing.domain.repository.PricingRepository

class FakePricingRepository : PricingRepository {
    private var pricingConfiguration = PricingConfiguration.defaults()
    private var priceListConfiguration = PriceListConfiguration.defaults()
    private val products = mutableMapOf<Long, PriceListProduct>()
    private var nextId = 1L

    override fun savePricingConfiguration(configuration: PricingConfiguration): PricingConfiguration {
        pricingConfiguration = configuration
        return pricingConfiguration
    }

    override fun getPricingConfiguration(): PricingConfiguration = pricingConfiguration

    override fun savePriceListConfiguration(configuration: PriceListConfiguration): PriceListConfiguration {
        priceListConfiguration = configuration
        return priceListConfiguration
    }

    override fun getPriceListConfiguration(): PriceListConfiguration = priceListConfiguration

    override fun saveProduct(product: PriceListProduct): PriceListProduct {
        val persisted = if (product.id == 0L) {
            product.copy(id = nextId++)
        } else {
            product
        }
        products[persisted.id] = persisted
        return persisted
    }

    override fun findAllProducts(): List<PriceListProduct> = products.values.sortedBy { it.name }
}
