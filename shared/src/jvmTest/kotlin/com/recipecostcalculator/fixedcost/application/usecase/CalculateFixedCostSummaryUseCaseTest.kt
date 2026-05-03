package com.recipecostcalculator.fixedcost.application.usecase

import com.recipecostcalculator.financial.domain.model.Money
import com.recipecostcalculator.fixedcost.FakeFixedCostRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class CalculateFixedCostSummaryUseCaseTest {
    private val repository = FakeFixedCostRepository()
    private val addFixedCostItemUseCase = AddFixedCostItemUseCase(repository)
    private val setProductionPerMonthUseCase = SetProductionPerMonthUseCase(repository)
    private val calculateFixedCostSummaryUseCase = CalculateFixedCostSummaryUseCase(repository)

    @Test
    fun `should calculate fixed cost per pizza using monthly production`() {
        addFixedCostItemUseCase.execute(
            AddFixedCostItemCommand(
                name = "Alquiler",
                monthlyAmount = Money.of("300000"),
            ),
        )
        addFixedCostItemUseCase.execute(
            AddFixedCostItemCommand(
                name = "Luz",
                monthlyAmount = Money.of("60000"),
            ),
        )
        setProductionPerMonthUseCase.execute(
            SetProductionPerMonthCommand(pizzasPerMonth = java.math.BigDecimal("1200")),
        )

        val summary = calculateFixedCostSummaryUseCase.execute()

        assertEquals("360000", summary.totalMonthlyFixedCost.amount.toPlainString())
        assertEquals("1200", summary.productionPerMonth.toPlainString())
        assertEquals("300", summary.fixedCostPerPizza.amount.toPlainString())
    }

    @Test
    fun `should return zero fixed cost per pizza when production is zero`() {
        addFixedCostItemUseCase.execute(
            AddFixedCostItemCommand(
                name = "Internet",
                monthlyAmount = Money.of("50000"),
            ),
        )

        val summary = calculateFixedCostSummaryUseCase.execute()

        assertEquals("50000", summary.totalMonthlyFixedCost.amount.toPlainString())
        assertEquals("0", summary.fixedCostPerPizza.amount.toPlainString())
    }
}
