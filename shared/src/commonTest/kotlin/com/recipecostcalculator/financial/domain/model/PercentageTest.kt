package com.recipecostcalculator.financial.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFailsWith

class PercentageTest {
    @Test
    fun init_validPercentage_acceptsZero() {
        val pct = Percentage(0.0)
        assertEquals(0.0, pct.value, 0.01)
    }

    @Test
    fun init_validPercentage_acceptsOne() {
        val pct = Percentage(1.0)
        assertEquals(1.0, pct.value, 0.01)
    }

    @Test
    fun init_validPercentage_acceptsHalf() {
        val pct = Percentage(0.5)
        assertEquals(0.5, pct.value, 0.01)
    }

    @Test
    fun init_invalidPercentage_throwsForNegative() {
        assertFailsWith<IllegalArgumentException> {
            Percentage(-0.1)
        }
    }

    @Test
    fun init_invalidPercentage_throwsForGreaterThanOne() {
        assertFailsWith<IllegalArgumentException> {
            Percentage(1.1)
        }
    }

    @Test
    fun complement_returnsOneMinusValue() {
        val pct = Percentage(0.3)
        assertEquals(0.7, pct.complement(), 0.01)
    }

    @Test
    fun complementIsZero_returnsTrueForOneHundredPercent() {
        val pct = Percentage(1.0)
        assertTrue(pct.complementIsZero())
    }

    @Test
    fun fromPercent_convertsCorrectly() {
        val pct = Percentage.fromPercent(50.0)
        assertEquals(0.5, pct.value, 0.01)
    }

    @Test
    fun toDouble_returnsValue() {
        val pct = Percentage(0.75)
        assertEquals(0.75, pct.toDouble(), 0.01)
    }
}