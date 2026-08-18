package com.example.valomobile.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class SkinItem @JvmOverloads constructor(
    val uuid: String,
    val displayName: String,
    val displayIcon: String,
    val weaponType: String,
    val price: Int,
    val discount: Int = 0,
    val tier: String = "Select",
    val skinUuid: String = uuid
) {
    val finalPrice: Int get() {
        val basePrice = if (price > 0) price else getPriceFromTier(tier)
        return basePrice - (basePrice * discount / 100)
    }

    private fun getPriceFromTier(tier: String): Int {
        return when {
            tier.contains("Select", ignoreCase = true) -> 875
            tier.contains("Deluxe", ignoreCase = true) -> 1275
            tier.contains("Premium", ignoreCase = true) -> 1775
            tier.contains("Ultra", ignoreCase = true) -> 2475
            else -> 0
        }
    }
}
