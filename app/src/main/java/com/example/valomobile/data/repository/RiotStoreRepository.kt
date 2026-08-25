package com.example.valomobile.data.repository

import android.util.Log
import com.example.valomobile.data.remote.RiotStoreApiService
import com.example.valomobile.data.remote.ValorantApiService
import com.example.valomobile.data.remote.model.*
import com.example.valomobile.domain.model.Bundle
import com.example.valomobile.domain.model.SkinItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RiotStoreRepository @Inject constructor(
    private val authRepository: RiotAuthRepository,
    private val storeApiService: RiotStoreApiService,
    private val valorantApiService: ValorantApiService,
    private val catalogRepository: SkinCatalogRepository
) {
    companion object {
        private const val TAG = "RiotStoreRepo"
    }

    private var cachedBundlesMeta: Map<String, ValorantBundleItem> = emptyMap()
    private var isMetadataLoaded = false

    private suspend fun ensureMetadata() = withContext(Dispatchers.IO) {
        if (!isMetadataLoaded) {
            try {
                catalogRepository.ensureAllCatalogMetadataLoaded()
                val bundlesResponse = valorantApiService.getBundles(language = "en-US")
                cachedBundlesMeta = bundlesResponse.data.associateBy { it.uuid.lowercase() }
                isMetadataLoaded = true
            } catch (e: Exception) {
                Log.w(TAG, "Failed to load metadata from valorant-api.com", e)
            }
        }
    }

    private suspend fun fetchStorefrontRaw(): RiotStorefrontRawResponse = withContext(Dispatchers.IO) {
        var accessToken = authRepository.getAccessToken()
        var entitlementsToken = authRepository.getEntitlementsToken()
        val puuid = authRepository.getPuuid()
            ?: throw IOException("Missing player PUUID.")
        val region = authRepository.getRegion()
        val clientVersion = authRepository.getClientVersion()

        val storefrontUrl = "https://pd.$region.a.pvp.net/store/v3/storefront/$puuid"

        if (accessToken.isNullOrBlank() || entitlementsToken.isNullOrBlank()) {
            val refreshed = authRepository.refreshSessionSilently()
            if (refreshed) {
                accessToken = authRepository.getAccessToken()
                entitlementsToken = authRepository.getEntitlementsToken()
            }
        }

        if (accessToken.isNullOrBlank() || entitlementsToken.isNullOrBlank()) {
            throw IOException("No active Riot login session.")
        }

        try {
            return@withContext storeApiService.getStorefront(
                url = storefrontUrl,
                authHeader = "Bearer $accessToken",
                entitlementsToken = entitlementsToken,
                clientVersion = clientVersion,
                clientPlatform = RiotAuthRepository.CLIENT_PLATFORM
            )
        } catch (e: Exception) {
            val isAuthError = e.message?.contains("400") == true 
                || e.message?.contains("401") == true 
                || e.message?.contains("BAD_CLAIMS") == true 
                || e.message?.contains("Unauthorized") == true

            if (isAuthError) {
                Log.w(TAG, "Storefront request failed with auth error, attempting silent refresh...", e)
                val refreshed = authRepository.refreshSessionSilently()
                if (refreshed) {
                    val newAccessToken = authRepository.getAccessToken()
                    val newEntitlementsToken = authRepository.getEntitlementsToken()
                    if (!newAccessToken.isNullOrBlank() && !newEntitlementsToken.isNullOrBlank()) {
                        try {
                            return@withContext storeApiService.getStorefront(
                                url = storefrontUrl,
                                authHeader = "Bearer $newAccessToken",
                                entitlementsToken = newEntitlementsToken,
                                clientVersion = authRepository.getClientVersion(),
                                clientPlatform = RiotAuthRepository.CLIENT_PLATFORM
                            )
                        } catch (retryEx: Exception) {
                            Log.e(TAG, "Storefront retry after refresh failed", retryEx)
                        }
                    }
                }
                Log.w(TAG, "Session expired and could not be refreshed. Logging out.")
                authRepository.logout()
                throw IOException("Riot session expired. Please log in again.", e)
            }

            Log.e(TAG, "Storefront API error", e)
            throw IOException("Failed to load store from Riot Games: ${e.message}", e)
        }
    }

    suspend fun getStoreRotation(): List<SkinItem> = withContext(Dispatchers.IO) {
        ensureMetadata()
        val raw = fetchStorefrontRaw()
        val singleOffers = raw.skinsPanelLayout?.singleItemOffers ?: emptyList()
        val singleStoreOffers = raw.skinsPanelLayout?.singleItemStoreOffers ?: emptyList()

        val offerCostMap = mutableMapOf<String, Int>()
        for (offer in singleStoreOffers) {
            val cost = extractCost(offer.cost, 0)
            offerCostMap[offer.offerId.lowercase()] = cost
        }

        val result = mutableListOf<SkinItem>()
        for (skinUuid in singleOffers) {
            val key = skinUuid.lowercase()
            val meta = catalogRepository.getItemMeta(key)

            val price = offerCostMap[key] ?: meta?.price ?: 1775
            val displayName = meta?.displayName ?: "Valorant Skin"
            val displayIcon = meta?.displayIcon ?: ""

            result.add(
                SkinItem(
                    uuid = skinUuid,
                    displayName = displayName,
                    displayIcon = displayIcon,
                    weaponType = meta?.itemType ?: "Weapon",
                    price = price,
                    discount = 0,
                    tier = meta?.tier ?: "Select",
                    skinUuid = meta?.uuid ?: skinUuid
                )
            )
        }
        result
    }

    suspend fun getFeaturedBundles(): List<Bundle> = withContext(Dispatchers.IO) {
        ensureMetadata()
        val raw = fetchStorefrontRaw()
        val bundleWrapper = raw.featuredBundle ?: return@withContext emptyList()
        val rawBundles = bundleWrapper.bundles.ifEmpty {
            bundleWrapper.bundle?.let { listOf(it) } ?: emptyList()
        }

        val result = mutableListOf<Bundle>()
        for (b in rawBundles) {
            val bundleUuid = b.dataAssetId ?: b.id ?: ""
            val bundleMeta = cachedBundlesMeta[bundleUuid.lowercase()]
            val bundleTitle = bundleMeta?.displayName
                ?.replace(" Bundle", "", ignoreCase = true)
                ?.replace(" Collection", "", ignoreCase = true)
                ?: "Exclusive"

            val items = mutableListOf<SkinItem>()
            val rawItems = b.itemOffers.ifEmpty { b.items }

            for (item in rawItems) {
                val reward = item.offer?.rewards?.firstOrNull() ?: item.item
                val itemTypeId = reward?.itemTypeId?.lowercase() ?: ""
                val itemUuid = reward?.itemId
                    ?: item.bundleItemOfferId
                    ?: item.itemId
                    ?: item.offer?.offerId
                    ?: ""

                val meta = catalogRepository.getItemMeta(itemUuid)

                val itemType = meta?.itemType ?: when {
                    itemTypeId.contains("3f296c07") -> "Player Card"
                    itemTypeId.contains("dd3bf334") -> "Gun Buddy"
                    itemTypeId.contains("dbe185c1") -> "Spray"
                    itemTypeId.contains("de7bf618") -> "Player Title"
                    itemTypeId.contains("b0254c7c") -> "Flex"
                    item.basePrice == 1350 || item.basePrice == 1000 -> "Flex"
                    item.basePrice == 375 -> "Player Card"
                    item.basePrice == 475 -> "Gun Buddy"
                    item.basePrice == 325 -> "Spray"
                    item.basePrice != null && item.basePrice!! > 1500 -> "Weapon Skin"
                    else -> "Flex"
                }

                val displayName = meta?.displayName ?: when (itemType) {
                    "Flex" -> "$bundleTitle Flex"
                    "Player Card" -> "$bundleTitle Card"
                    "Gun Buddy" -> "$bundleTitle Buddy"
                    "Spray" -> "$bundleTitle Spray"
                    "Player Title" -> "$bundleTitle Title"
                    else -> "$bundleTitle Item"
                }

                val displayIcon = meta?.displayIcon?.ifBlank { null }
                    ?: bundleMeta?.displayIcon
                    ?: bundleMeta?.verticalPromoImage
                    ?: ""

                val defaultBasePrice = meta?.price ?: when (itemType) {
                    "Player Card" -> 375
                    "Gun Buddy" -> 475
                    "Spray" -> 325
                    "Player Title" -> 200
                    "Flex" -> 1350
                    else -> 1775
                }

                val rawBasePrice = extractCost(item.offer?.cost, item.basePrice ?: defaultBasePrice)
                val basePrice = if (rawBasePrice > 0) rawBasePrice else defaultBasePrice

                items.add(
                    SkinItem(
                        uuid = itemUuid,
                        displayName = displayName,
                        displayIcon = displayIcon,
                        weaponType = itemType,
                        price = basePrice,
                        discount = 0,
                        tier = meta?.tier ?: "Select",
                        skinUuid = meta?.uuid ?: itemUuid
                    )
                )
            }

            var totalPrice = extractCost(b.totalDiscountedCost, 0)
            if (totalPrice == 0) {
                totalPrice = extractCost(b.totalBaseCost, 0)
            }
            if (totalPrice == 0 && items.isNotEmpty()) {
                totalPrice = items.sumOf { it.finalPrice }
            }

            result.add(
                Bundle(
                    uuid = bundleUuid,
                    displayName = bundleMeta?.displayName ?: "Valorant Bundle",
                    displayIcon = bundleMeta?.displayIcon ?: bundleMeta?.verticalPromoImage ?: "",
                    description = bundleMeta?.description ?: "",
                    price = totalPrice,
                    items = items
                )
            )
        }
        result
    }

    suspend fun getNightMarket(): List<SkinItem> = withContext(Dispatchers.IO) {
        ensureMetadata()
        val raw = fetchStorefrontRaw()
        val bonusStore = raw.bonusStore ?: return@withContext emptyList()
        val offers = bonusStore.bonusStoreOffers

        val result = mutableListOf<SkinItem>()
        for (offer in offers) {
            val rewards = offer.offer?.rewards ?: emptyList()
            val itemUuid = rewards.firstOrNull()?.itemId ?: offer.bonusOfferId ?: ""
            val key = itemUuid.lowercase()
            val meta = catalogRepository.getItemMeta(key)

            val basePrice = extractCost(offer.offer?.cost, 1775)
            val discountCost = extractCost(offer.discountCosts, basePrice)
            val discount = offer.discountPercent

            result.add(
                SkinItem(
                    uuid = itemUuid,
                    displayName = meta?.displayName ?: "Night Market Skin",
                    displayIcon = meta?.displayIcon ?: "",
                    weaponType = meta?.itemType ?: "Weapon",
                    price = basePrice,
                    discount = discount,
                    tier = meta?.tier ?: "Select",
                    skinUuid = meta?.uuid ?: itemUuid
                )
            )
        }
        result
    }

    suspend fun getWallet(): UserWallet = withContext(Dispatchers.IO) {
        val accessToken = authRepository.getAccessToken()
        val entitlementsToken = authRepository.getEntitlementsToken()
        val puuid = authRepository.getPuuid() ?: return@withContext UserWallet()
        val region = authRepository.getRegion()
        val clientVersion = authRepository.getClientVersion()

        val walletUrl = "https://pd.$region.a.pvp.net/store/v1/wallet/$puuid"

        try {
            val raw = storeApiService.getWallet(
                url = walletUrl,
                authHeader = "Bearer $accessToken",
                entitlementsToken = entitlementsToken ?: "",
                clientVersion = clientVersion,
                clientPlatform = RiotAuthRepository.CLIENT_PLATFORM
            )
            val balances = raw.balances
            val vp = balances["85ad13f7-3d1b-5128-9eb2-7cd8ee0b5741"] 
                ?: balances.entries.firstOrNull { it.key.startsWith("85ad13f7", ignoreCase = true) }?.value 
                ?: 0
            val radianite = balances["e59aa87c-4cbf-517a-5983-6e81511be9b7"] 
                ?: balances.entries.firstOrNull { it.key.startsWith("e59aa87c", ignoreCase = true) }?.value 
                ?: 0
            val kingdomCredits = balances["85ca954a-41f2-ce94-9b45-8ca3dd39a00d"] 
                ?: balances.entries.firstOrNull { it.key.startsWith("85ca954a", ignoreCase = true) }?.value 
                ?: 0
            UserWallet(vp = vp, radianite = radianite, kingdomCredits = kingdomCredits)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to fetch user wallet balances", e)
            UserWallet()
        }
    }

    private fun extractCost(costObj: Any?, fallback: Int): Int {
        if (costObj == null) return fallback
        if (costObj is Number) return costObj.toInt()
        if (costObj is Map<*, *>) {
            for (value in costObj.values) {
                if (value is Number) return value.toInt()
            }
        }
        return fallback
    }
}
