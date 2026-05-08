package com.recipecostcalculator.financial.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFailsWith

class MoneyTest {
    @Test
    fun plus_addsTwoMoneyValues() {
        val a = Money.of(100.50)
        val b = Money.of(50.25)
        val result = a + b
        assertEquals(150.75, result.amount, 0.01)
    }

    @Test
    fun minus_subtractsMoneyValues() {
        val a = Money.of(100.0)
        val b = Money.of(30.0)
        val result = a - b
        assertEquals(70.0, result.amount, 0.01)
    }

    @Test
    fun times_multipliesByDouble() {
        val money = Money.of(100.0)
        val result = money * 1.15
        assertEquals(115.0, result.amount, 0.01)
    }

    @Test
    fun times_multipliesByInt() {
        val money = Money.of(50.0)
        val result = money * 3
        assertEquals(150.0, result.amount, 0.01)
    }

    @Test
    fun div_dividesByDouble() {
        val money = Money.of(100.0)
        val result = money / 2.0
        assertEquals(50.0, result.amount, 0.01)
    }

    @Test
    fun div_dividesByInt() {
        val money = Money.of(90.0)
        val result = money / 3
        assertEquals(30.0, result.amount, 0.01)
    }

    @Test
    fun div_byZero_throwsException() {
        val money = Money.of(100.0)
        assertFailsWith<IllegalArgumentException> {
            money / 0.0
        }
    }

    @Test
    fun unaryMinus_negatesValue() {
        val money = Money.of(100.0)
        val result = -money
        assertEquals(-100.0, result.amount, 0.01)
    }

    @Test
    fun isZero_returnsTrueForZero() {
        val money = Money.ZERO
        assertTrue(money.isZero())
    }

    @Test
    fun of_roundsToTwoDecimals() {
        val money = Money.of(100.556)
        assertEquals(100.56, money.amount, 0.01)
    }
}