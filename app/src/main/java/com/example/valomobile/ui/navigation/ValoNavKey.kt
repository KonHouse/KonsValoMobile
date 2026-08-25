package com.example.valomobile.ui.navigation

import androidx.navigation3.runtime.NavKey
import com.example.valomobile.domain.model.SkinItem
import kotlinx.serialization.Serializable

sealed interface ValoNavKey : NavKey {
    @Serializable
    data object Connect : ValoNavKey

    @Serializable
    data object StoreRotation : ValoNavKey

    @Serializable
    data object Bundles : ValoNavKey

    @Serializable
    data object NightMarket : ValoNavKey

    @Serializable
    data object Catalog : ValoNavKey

    @Serializable
    data object Wishlist : ValoNavKey

    @Serializable
    data object VpCalculator : ValoNavKey

    @Serializable
    data object Settings : ValoNavKey

    @Serializable
    data class StoreDetail(val item: SkinItem) : ValoNavKey
}
