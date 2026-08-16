package com.recipecostcalculator.domain.model

import com.recipecostcalculator.financial.domain.model.Money
import com.recipecostcalculator.financial.domain.model.Quantity
import com.recipecostcalculator.measurement.MeasurementDimension
import com.recipecostcalculator.measurement.MeasurementUnit
import kotlin.test.Test
import kotlin.test.assertEquals

class IngredientTest {

    @Test
    fun unitCost_harinaBolsa25kg_dividesPrecioSobreContenido() {
        val harina = Ingredient(
            name = "Harina",
            dimension = MeasurementDimension.MASS,
            purchaseUnit = MeasurementUnit.KG,
            purchasePrice = Money(18500.0),
            contentAmount = Quantity(25.0, MeasurementUnit.KG),
            usageUnit = MeasurementUnit.KG
        )

        assertEquals(740.0, harina.unitCost().amount, 0.01)
    }

    @Test
    fun unitCost_mozzarellaHorma1kg_precioIgualAlContenidoUnitario() {
        val mozzarella = Ingredient(
            name = "Mozzarella",
            dimension = MeasurementDimension.MASS,
            purchaseUnit = MeasurementUnit.KG,
            purchasePrice = Money(9300.0),
            contentAmount = Quantity(1.0, MeasurementUnit.KG),
            usageUnit = MeasurementUnit.KG
        )

        assertEquals(9300.0, mozzarella.unitCost().amount, 0.01)
    }

    @Test
    fun unitCost_aceiteBotella900cc_calculaCentavosPorCc() {
        val aceite = Ingredient(
            name = "Aceite",
            dimension = MeasurementDimension.VOLUME,
            purchaseUnit = MeasurementUnit.CC,
            purchasePrice = Money(3000.0),
            contentAmount = Quantity(900.0, MeasurementUnit.CC),
            usageUnit = MeasurementUnit.CC
        )

        assertEquals(3.33, aceite.unitCost().amount, 0.01)
    }

    @Test
    fun unitCost_levaduraSobre500g_calculaCentavosPorGramo() {
        val levadura = Ingredient(
            name = "Levadura",
            dimension = MeasurementDimension.MASS,
            purchaseUnit = MeasurementUnit.G,
            purchasePrice = Money(3800.0),
            contentAmount = Quantity(500.0, MeasurementUnit.G),
            usageUnit = MeasurementUnit.G
        )

        assertEquals(7.6, levadura.unitCost().amount, 0.01)
    }

    @Test
    fun unitCost_salsaLataUnica_esPrecioPorLata() {
        val salsa = Ingredient(
            name = "Salsa",
            dimension = MeasurementDimension.COUNT,
            purchaseUnit = MeasurementUnit.UN,
            purchasePrice = Money(930.0),
            contentAmount = Quantity(1.0, MeasurementUnit.UN),
            usageUnit = MeasurementUnit.UN
        )

        assertEquals(930.0, salsa.unitCost().amount, 0.01)
    }

    @Test
    fun unitCost_compraEnKgUsoEnGramos_convierteAntesDeDividir() {
        val ingrediente = Ingredient(
            name = "Sal",
            dimension = MeasurementDimension.MASS,
            purchaseUnit = MeasurementUnit.KG,
            purchasePrice = Money(20000.0),
            contentAmount = Quantity(2.0, MeasurementUnit.KG),
            usageUnit = MeasurementUnit.G
        )

        assertEquals(10.0, ingrediente.unitCost().amount, 0.01)
    }

    @Test
    fun unitCost_pescado10000kgPor2kg_regresionReporte210_esDiezMilPorKilo() {
        val pescado = Ingredient(
            name = "Pescado",
            dimension = MeasurementDimension.MASS,
            purchaseUnit = MeasurementUnit.KG,
            purchasePrice = Money(20000.0),
            contentAmount = Quantity(2.0, MeasurementUnit.KG),
            usageUnit = MeasurementUnit.KG
        )

        assertEquals(10000.0, pescado.unitCost().amount, 0.01)
    }
}
