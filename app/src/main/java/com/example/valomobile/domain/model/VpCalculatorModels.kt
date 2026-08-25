package com.example.valomobile.domain.model

data class VpPack(
    val baseVp: Int,
    val bonusVp: Int,
    val totalVp: Int,
    val pricePln: Double,
    val priceEur: Double
) {
    val displayName: String
        get() = "$totalVp VP"
}

data class SelectedVpPack(
    val pack: VpPack,
    val quantity: Int
)

enum class CurrencyType(val symbol: String, val code: String) {
    PLN("zł", "PLN"),
    EUR("€", "EUR")
}

data class VpCalculationResult(
    val totalCostVp: Int = 0,
    val currentVp: Int = 0,
    val missingVp: Int = 0,
    val recommendedPacks: List<SelectedVpPack> = emptyList(),
    val totalPackVp: Int = 0,
    val totalCostPln: Double = 0.0,
    val totalCostEur: Double = 0.0,
    val leftoverVp: Int = 0,
    val isAlreadyAffordable: Boolean = true
)

object VpPacksCatalog {
    // Official Riot Games Valorant VP Packages (Live In-Game Poland PLN Pricing)
    // Ordered from largest to smallest for optimal search pruning
    val PACKS = listOf(
        VpPack(baseVp = 9525, bonusVp = 1475, totalVp = 11000, pricePln = 427.99, priceEur = 99.99),
        VpPack(baseVp = 4750, bonusVp = 600, totalVp = 5350, pricePln = 213.99, priceEur = 49.99),
        VpPack(baseVp = 3325, bonusVp = 325, totalVp = 3650, pricePln = 149.99, priceEur = 34.99),
        VpPack(baseVp = 1900, bonusVp = 150, totalVp = 2050, pricePln = 85.49, priceEur = 19.99),
        VpPack(baseVp = 950, bonusVp = 50, totalVp = 1000, pricePln = 42.99, priceEur = 9.99),
        VpPack(baseVp = 475, bonusVp = 0, totalVp = 475, pricePln = 21.49, priceEur = 4.99)
    )

    /**
     * Finds the most cost-effective combination of VP packs that covers [missingVp].
     * 1. Minimizes the real-money cost (PLN/EUR in exact integer cents).
     * 2. Ties broken by maximizing total VP obtained (giving the user the maximum value).
     * 3. Further ties broken by minimizing the number of packs to buy.
     */
    fun calculateOptimalPacks(
        totalCostVp: Int,
        currentVp: Int,
        currency: CurrencyType = CurrencyType.PLN
    ): VpCalculationResult {
        val missingVp = (totalCostVp - currentVp).coerceAtLeast(0)

        if (missingVp == 0) {
            return VpCalculationResult(
                totalCostVp = totalCostVp,
                currentVp = currentVp,
                missingVp = 0,
                recommendedPacks = emptyList(),
                totalPackVp = 0,
                totalCostPln = 0.0,
                totalCostEur = 0.0,
                leftoverVp = currentVp - totalCostVp,
                isAlreadyAffordable = true
            )
        }

        val packs = PACKS
        val pricesInCents = packs.map { pack ->
            if (currency == CurrencyType.PLN) Math.round(pack.pricePln * 100).toInt()
            else Math.round(pack.priceEur * 100).toInt()
        }

        var bestCostCents = Int.MAX_VALUE
        var bestTotalVp = -1
        var bestPackCounts = IntArray(packs.size)

        fun search(
            pIdx: Int,
            accumulatedVp: Int,
            accumulatedCostCents: Int,
            currentCounts: IntArray
        ) {
            if (accumulatedVp >= missingVp) {
                val isBetter = when {
                    accumulatedCostCents < bestCostCents -> true
                    accumulatedCostCents == bestCostCents && accumulatedVp > bestTotalVp -> true
                    accumulatedCostCents == bestCostCents && accumulatedVp == bestTotalVp && currentCounts.sum() < bestPackCounts.sum() -> true
                    else -> false
                }
                if (isBetter) {
                    bestCostCents = accumulatedCostCents
                    bestTotalVp = accumulatedVp
                    bestPackCounts = currentCounts.clone()
                }
                return
            }

            // Pruning if current branch already exceeds bestCost
            if (accumulatedCostCents >= bestCostCents) return
            if (pIdx >= packs.size) return

            val pack = packs[pIdx]
            val packPrice = pricesInCents[pIdx]

            val remainingNeeded = missingVp - accumulatedVp
            val maxCount = (remainingNeeded / pack.totalVp) + 1

            for (count in maxCount downTo 0) {
                currentCounts[pIdx] = count
                search(
                    pIdx = pIdx + 1,
                    accumulatedVp = accumulatedVp + count * pack.totalVp,
                    accumulatedCostCents = accumulatedCostCents + count * packPrice,
                    currentCounts = currentCounts
                )
            }
            currentCounts[pIdx] = 0
        }

        search(
            pIdx = 0,
            accumulatedVp = 0,
            accumulatedCostCents = 0,
            currentCounts = IntArray(packs.size)
        )

        val recommendedPacks = packs.mapIndexedNotNull { index, pack ->
            val count = bestPackCounts[index]
            if (count > 0) SelectedVpPack(pack = pack, quantity = count) else null
        }

        val totalPackVp = recommendedPacks.sumOf { it.pack.totalVp * it.quantity }
        val totalCostPln = recommendedPacks.sumOf { it.pack.pricePln * it.quantity }
        val totalCostEur = recommendedPacks.sumOf { it.pack.priceEur * it.quantity }
        val leftoverVp = (currentVp + totalPackVp) - totalCostVp

        return VpCalculationResult(
            totalCostVp = totalCostVp,
            currentVp = currentVp,
            missingVp = missingVp,
            recommendedPacks = recommendedPacks,
            totalPackVp = totalPackVp,
            totalCostPln = totalCostPln,
            totalCostEur = totalCostEur,
            leftoverVp = leftoverVp,
            isAlreadyAffordable = false
        )
    }
}
