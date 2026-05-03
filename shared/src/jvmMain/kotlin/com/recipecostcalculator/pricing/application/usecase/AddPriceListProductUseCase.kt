package com.recipecostcalculator.pricing.application.usecase

import com.recipecostcalculator.pricing.domain.model.PriceListProduct
import com.recipecostcalculator.pricing.domain.repository.PricingRepository

class AddPriceListProductUseCase(
    private val pricingRepository: PricingRepository,
) {
    fun execute(command: AddPriceListProductCommand): PriceListProduct {
        val product = PriceListProduct(
            name = command.name.trim(),
            sellingPrice = command.sellingPrice,
            notes = command.notes.trim(),
        )
        return pricingRepository.saveProduct(product)
    }
}
