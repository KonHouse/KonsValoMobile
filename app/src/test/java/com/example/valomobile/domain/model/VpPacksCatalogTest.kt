package com.example.valomobile.domain.model

import org.junit.Assert.*
import org.junit.Test

class VpPacksCatalogTest {

    @Test
    fun calculateOptimalPacks_whenCurrentVpIsSufficient_returnsAffordable() {
        val result = VpPacksCatalog.calculateOptimalPacks(
            totalCostVp = 1775,
            currentVp = 2000,
            currency = CurrencyType.PLN
        )

        assertTrue(result.isAlreadyAffordable)
        assertEquals(0, result.missingVp)
        assertEquals(225, result.leftoverVp)
        assertTrue(result.recommendedPacks.isEmpty())
        assertEquals(0.0, result.totalCostPln, 0.001)
    }

    @Test
    fun calculateOptimalPacks_exactMatchSinglePack_returnsCorrectPack() {
        val result = VpPacksCatalog.calculateOptimalPacks(
            totalCostVp = 1000,
            currentVp = 0,
            currency = CurrencyType.PLN
        )

        assertFalse(result.isAlreadyAffordable)
        assertEquals(1000, result.missingVp)
        assertEquals(1, result.recommendedPacks.size)
        assertEquals(1000, result.recommendedPacks[0].pack.totalVp)
        assertEquals(1, result.recommendedPacks[0].quantity)
        assertEquals(42.99, result.totalCostPln, 0.001)
        assertEquals(0, result.leftoverVp)
    }

    @Test
    fun calculateOptimalPacks_standardSkin1775Vp_optimizesCost() {
        val result = VpPacksCatalog.calculateOptimalPacks(
            totalCostVp = 1775,
            currentVp = 0,
            currency = CurrencyType.PLN
        )

        assertFalse(result.isAlreadyAffordable)
        assertEquals(1775, result.missingVp)
        // 2050 VP pack is 85.49 zł vs 2x 1000 VP = 85.98 zł
        // 2050 VP is cheaper and provides more VP!
        val totalVp = result.recommendedPacks.sumOf { it.pack.totalVp * it.quantity }
        assertTrue(totalVp >= 1775)
        assertEquals(85.49, result.totalCostPln, 0.001)
    }

    @Test
    fun calculateOptimalPacks_tieBreaker_maximizesBonusVp() {
        // Test when 5350 VP pack (213.99) vs combinations
        val result = VpPacksCatalog.calculateOptimalPacks(
            totalCostVp = 5350,
            currentVp = 0,
            currency = CurrencyType.PLN
        )

        assertEquals(1, result.recommendedPacks.size)
        assertEquals(5350, result.recommendedPacks[0].pack.totalVp)
        assertEquals(213.99, result.totalCostPln, 0.001)
        assertEquals(0, result.leftoverVp)
    }
}
