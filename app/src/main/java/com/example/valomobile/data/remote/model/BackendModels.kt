package com.example.valomobile.data.remote.model

import com.google.gson.annotations.SerializedName

// --- Auth Requests & Responses ---

data class LoginRequest(
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String
)

data class TwoFaRequest(
    @SerializedName("code") val code: String
)

data class TokenUrlRequest(
    @SerializedName("redirectUrl") val redirectUrl: String,
    @SerializedName("url") val url: String = redirectUrl
)

data class BackendAuthResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String? = null,
    @SerializedName("bearerToken") val bearerToken: String? = null,
    @SerializedName("user") val user: BackendUser? = null
)

data class BackendStatusResponse(
    @SerializedName("isLoggedIn") val isLoggedIn: Boolean = false,
    @SerializedName("status") val status: String? = null,
    @SerializedName("bearerToken") val bearerToken: String? = null,
    @SerializedName("user") val user: BackendUser? = null
)

data class BackendUser(
    @SerializedName("gameName") val gameName: String? = null,
    @SerializedName("tagLine") val tagLine: String? = null,
    @SerializedName("region") val region: String? = null,
    @SerializedName("puuid") val puuid: String? = null
)

// --- Store & Catalog Models ---

data class BackendStoreResponse(
    @SerializedName("store_rotation") val storeRotation: List<BackendSkinItem> = emptyList(),
    @SerializedName("featured_bundles") val featuredBundles: List<BackendBundle> = emptyList(),
    @SerializedName("night_market") val nightMarket: BackendNightMarket? = null
)

data class BackendNightMarket(
    @SerializedName("is_active") val isActive: Boolean = false,
    @SerializedName("expires_at") val expiresAt: String? = null,
    @SerializedName("offers") val offers: List<BackendSkinItem> = emptyList()
)

data class BackendSkinItem(
    @SerializedName("uuid") val uuid: String,
    @SerializedName("display_name") val displayName: String,
    @SerializedName("display_icon") val displayIcon: String,
    @SerializedName("price") val price: Int,
    @SerializedName("discount_percent") val discountPercent: Int = 0
)

data class BackendBundle(
    @SerializedName("uuid") val uuid: String,
    @SerializedName("display_name") val displayName: String,
    @SerializedName("display_icon") val displayIcon: String,
    @SerializedName("description") val description: String? = null,
    @SerializedName("price") val price: Int,
    @SerializedName("items") val items: List<BackendSkinItem> = emptyList()
)
