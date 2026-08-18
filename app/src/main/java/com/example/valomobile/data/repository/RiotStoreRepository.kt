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
    private val storeApiService: RiotStoreApiService,
    private val authRepository: RiotAuthRepository,
    private val valorantApiService: ValorantApiService,
    private val catalogRepository: SkinCatalogRepository
) {
    private val currencyVpId = "85ad3983-4cc1-4528-ab8b-577392ee0fa0"
    private val currencyAltId = "85ad13f7-3d1b-5128-9eb2-7cd8ee0b5741"

    private var cachedBundlesMeta: Map<String, ValorantBundleItem> = emptyMap()

    private suspend fun ensureMetadata() {
        catalogRepository.getAllSkins()
        if (cachedBundlesMeta.isEmpty()) {
            try {
                val bundlesRes = valorantApiService.getBundles(language = "en-US")
                cachedBundlesMeta = bundlesRes.data.associateBy { it.uuid.lowercase() }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to load bundles metadata", e)
            }
        }
    }

    private suspend fun fetchStorefrontRaw(): RiotStorefrontRawResponse = withContext(Dispatchers.IO) {
        val accessToken = authRepository.getAccessToken()
            ?: throw IOException("No active Riot login session.")
        val entitlementsToken = authRepository.getEntitlementsToken()
            ?: throw IOException("Missing entitlements token.")
        val puuid = authRepository.getPuuid()
            ?: throw IOException("Missing player PUUID.")
        val region = authRepository.getRegion()
        val clientVersion = authRepository.getClientVersion()

        val storefrontUrl = "https://pd.$region.a.pvp.net/store/v3/storefront/$puuid"

        try {
            storeApiService.getStorefront(
                url = storefrontUrl,
                authHeader = "Bearer $accessToken",
                entitlementsToken = entitlementsToken,
                clientVersion = clientVersion,
                clientPlatform = RiotAuthRepository.CLIENT_PLATFORM
            )
        } catch (e: Exception) {
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

        val allCatalogSkins = catalogRepository.getAllSkins().associateBy { it.uuid.lowercase() }
        val levelToSkinMap = catalogRepository.getLevelToSkinMap()

        val result = mutableListOf<SkinItem>()
        for (skinUuid in singleOffers) {
            val key = skinUuid.lowercase()
            val baseSkinUuid = levelToSkinMap[key] ?: key
            val meta = allCatalogSkins[baseSkinUuid]

            val price = offerCostMap[key] ?: meta?.price ?: 1775
            val displayName = meta?.displayName ?: "Valorant Skin"
            val displayIcon = meta?.displayIcon ?: ""

            result.add(
                SkinItem(
                    uuid = skinUuid,
                    displayName = displayName,
                    displayIcon = displayIcon,
                    weaponType = meta?.weaponType ?: "Weapon",
                    price = price,
                    discount = 0,
                    tier = meta?.tier ?: "Select",
                    skinUuid = baseSkinUuid
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

        val allCatalogSkins = catalogRepository.getAllSkins().associateBy { it.uuid.lowercase() }
        val levelToSkinMap = catalogRepository.getLevelToSkinMap()

        val result = mutableListOf<Bundle>()
        for (b in rawBundles) {
            val bundleUuid = b.dataAssetId ?: b.id ?: ""
            val bundleMeta = cachedBundlesMeta[bundleUuid.lowercase()]

            val items = mutableListOf<SkinItem>()
            val rawItems = b.itemOffers.ifEmpty { b.items }

            for (item in rawItems) {
                val itemUuid = item.offer?.rewards?.firstOrNull()?.itemId
                    ?: item.item?.itemId
                    ?: item.bundleItemOfferId
                    ?: item.itemId
                    ?: item.offer?.offerId
                    ?: ""

                val key = itemUuid.lowercase()
                val baseSkinUuid = levelToSkinMap[key] ?: key
                val skinMeta = allCatalogSkins[baseSkinUuid]

                val basePrice = extractCost(item.offer?.cost, item.basePrice ?: 1775)
                val discountedCost = item.discountedCost?.let { extractCost(it, basePrice) } ?: item.discountedPrice ?: basePrice
                val discount = if (item.discountPercent > 0) {
                    (item.discountPercent * 100).toInt()
                } else if (basePrice > 0) {
                    ((basePrice - discountedCost) * 100 / basePrice)
                } else 0

                items.add(
                    SkinItem(
                        uuid = itemUuid,
                        displayName = skinMeta?.displayName ?: "Bundle Item",
                        displayIcon = skinMeta?.displayIcon ?: "",
                        weaponType = skinMeta?.weaponType ?: "Weapon",
                        price = basePrice,
                        discount = discount,
                        tier = skinMeta?.tier ?: "Select",
                        skinUuid = baseSkinUuid
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
        if (offers.isEmpty()) return@withContext emptyList()

        val allCatalogSkins = catalogRepository.getAllSkins().associateBy { it.uuid.lowercase() }
        val levelToSkinMap = catalogRepository.getLevelToSkinMap()

        val result = mutableListOf<SkinItem>()
        for (offer in offers) {
            val skinUuid = offer.offer?.rewards?.firstOrNull()?.itemId 
                ?: offer.offer?.offerId 
                ?: offer.bonusOfferId 
                ?: ""

            val key = skinUuid.lowercase()
            val baseSkinUuid = levelToSkinMap[key] ?: key
            val meta = allCatalogSkins[baseSkinUuid]

            val originalPrice = extractCost(offer.offer?.cost, 1775)
            val discountPercent = offer.discountPercent
            val discountedPrice = extractCost(offer.discountCosts, originalPrice - (originalPrice * discountPercent / 100))

            result.add(
                SkinItem(
                    uuid = skinUuid,
                    displayName = meta?.displayName ?: "Night Market Skin",
                    displayIcon = meta?.displayIcon ?: "",
                    weaponType = meta?.weaponType ?: "Weapon",
                    price = originalPrice,
                    discount = discountPercent,
                    tier = meta?.tier ?: "Select",
                    skinUuid = baseSkinUuid
                )
            )
        }
        result
    }

    private fun extractCost(costMap: Map<String, Int>?, fallback: Int): Int {
        if (costMap == null || costMap.isEmpty()) return fallback
        return costMap[currencyVpId] ?: costMap[currencyAltId] ?: costMap.values.firstOrNull() ?: fallback
    }

    companion object {
        private const val TAG = "RiotStoreRepository"
    }
}
