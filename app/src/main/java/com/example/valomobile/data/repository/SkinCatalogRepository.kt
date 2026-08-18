package com.example.valomobile.data.repository

import com.example.valomobile.data.remote.ValorantApiService
import com.example.valomobile.data.remote.model.ValorantSkin
import com.example.valomobile.domain.model.SkinItem
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SkinCatalogRepository @Inject constructor(
    private val apiService: ValorantApiService
) {
    private var cachedTiers: Map<String, String> = emptyMap()
    private var cachedSkins: List<SkinItem> = emptyList()
    private var levelToSkinMap: Map<String, String> = emptyMap()

    suspend fun getAllSkins(): List<SkinItem> {
        if (cachedSkins.isNotEmpty()) return cachedSkins

        if (cachedTiers.isEmpty()) {
            val tiersResponse = apiService.getContentTiers(language = "en-US")
            cachedTiers = tiersResponse.data.associate { it.uuid to it.displayName }
        }

        val response = apiService.getSkins(language = "en-US")
        
        // Build level to skin mapping
        levelToSkinMap = response.data.flatMap { skin ->
            skin.levels.map { level -> level.uuid to skin.uuid }
        }.toMap()

        cachedSkins = response.data
            .filter { it.displayIcon != null } // Only show skins with icons
            .map { skin ->
                val tier = cachedTiers[skin.contentTierUuid] ?: "Select"
                SkinItem(
                    uuid = skin.uuid,
                    displayName = skin.displayName,
                    displayIcon = skin.displayIcon ?: "",
                    weaponType = "Weapon",
                    price = getPriceFromTier(tier),
                    discount = 0,
                    tier = tier,
                    skinUuid = skin.uuid
                )
            }
        return cachedSkins
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

    suspend fun getSkinUuidForLevel(levelUuid: String): String {
        if (levelToSkinMap.isEmpty()) {
            getAllSkins()
        }
        return levelToSkinMap[levelUuid] ?: levelUuid
    }

    suspend fun getLevelToSkinMap(): Map<String, String> {
        if (levelToSkinMap.isEmpty()) {
            getAllSkins()
        }
        return levelToSkinMap
    }
}
