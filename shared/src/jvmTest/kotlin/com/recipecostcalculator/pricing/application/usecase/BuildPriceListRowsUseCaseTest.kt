package com.recipecostcalculator.pricing.application.usecase

import com.recipecostcalculator.financial.domain.model.Money
import com.recipecostcalculator.pricing.FakePricingRepository
import com.recipecostcalculator.pricing.domain.model.CostBasisType
import kotlin.test.Test
import kotlin.test.assertEquals

class BuildPriceListRowsUseCaseTest {
    private val repository = FakePricingRepository()
    private val addPriceListProductUseCase = AddPriceListProductUseCase(repository)
    private val setPriceListCostBasisUseCase = SetPriceListCostBasisUseCase(repository)
    private val buildPriceListRowsUseCase = BuildPriceListRowsUseCase(repository)

    @Test
    fun `should build rows using variable cost basis`() {
        addPriceListProductUseCase.execute(
            AddPriceListProductCommand(
                name = "Pizza muzza",
                sellingPrice = Money.of("500"),
                notes = "",
            ),
        )

        setPriceListCostBasisUseCase.execute(
            SetPriceListCostBasisCommand(costBasisType = CostBasisType.VARIABLE),
        )

        val rows = buildPriceListRowsUseCase.execute(
            variableCostPerPizza = Money.of("300"),
            totalCostPerPizza = Money.of("350"),
        )

        assertEquals(1, rows.size)
        assertEquals("300.00 ARS", rows[0].costUsed)
        assertEquals("200.00 ARS", rows[0].grossProfit)
        assertEquals("40.00", rows[0].marginPercent)
    }

    @Test
    fun `should build rows using total cost basis`() {
        addPriceListProductUseCase.execute(
            AddPriceListProductCommand(
                name = "Pizza especial",
                sellingPrice = Money.of("700"),
                notes = "Promo",
            ),
        )

        setPriceListCostBasisUseCase.execute(
            SetPriceListCostBasisCommand(costBasisType = CostBasisType.TOTAL),
        )

        val rows = buildPriceListRowsUseCase.execute(
            variableCostPerPizza = Money.of("300"),
            totalCostPerPizza = Money.of("420"),
        )

        assertEquals(1, rows.size)
        assertEquals("420.00 ARS", rows[0].costUsed)
        assertEquals("280.00 ARS", rows[0].grossProfit)
        assertEquals("40.00", rows[0].marginPercent)
        assertEquals("Promo", rows[0].notes)
    }
}
