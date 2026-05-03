package com.recipecostcalculator.pricing.domain.repository

import com.recipecostcalculator.pricing.domain.model.PriceListConfiguration
import com.recipecostcalculator.pricing.domain.model.PriceListProduct
import com.recipecostcalculator.pricing.domain.model.PricingConfiguration

interface PricingRepository {
    fun savePricingConfiguration(configuration: PricingConfiguration): PricingConfiguration
    fun getPricingConfiguration(): PricingConfiguration
    fun savePriceListConfiguration(configuration: PriceListConfiguration): PriceListConfiguration
    fun getPriceListConfiguration(): PriceListConfiguration
    fun saveProduct(product: PriceListProduct): PriceListProduct
    fun findAllProducts(): List<PriceListProduct>
}
