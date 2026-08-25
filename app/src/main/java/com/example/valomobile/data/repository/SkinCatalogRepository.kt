package com.example.valomobile.data.repository

import com.example.valomobile.data.remote.ValorantApiService
import com.example.valomobile.data.remote.model.CatalogItemMeta
import com.example.valomobile.data.remote.model.ValorantSkin
import com.example.valomobile.domain.model.SkinItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SkinCatalogRepository @Inject constructor(
    private val apiService: ValorantApiService
) {
    private var cachedSkins: List<SkinItem> = emptyList()
    private var cachedRawSkins: Map<String, ValorantSkin> = emptyMap()
    private var cachedTiers: Map<String, String> = emptyMap()
    private var levelToSkinMap: Map<String, String> = emptyMap()
    private var allCatalogItemMetaMap: Map<String, CatalogItemMeta> = emptyMap()

    suspend fun ensureAllCatalogMetadataLoaded() = withContext(Dispatchers.IO) {
        if (allCatalogItemMetaMap.isNotEmpty()) return@withContext

        val tiersDeferred = async {
            try {
                apiService.getContentTiers().data.associate { it.uuid to it.displayName }
            } catch (e: Exception) {
                emptyMap()
            }
        }

        val skinsDeferred = async {
            try {
                apiService.getSkins(language = "en-US").data
            } catch (e: Exception) {
                emptyList()
            }
        }

        val cardsDeferred = async {
            try {
                apiService.getPlayerCards(language = "en-US").data
            } catch (e: Exception) {
                emptyList()
            }
        }

        val buddiesDeferred = async {
            try {
                apiService.getBuddies(language = "en-US").data
            } catch (e: Exception) {
                emptyList()
            }
        }

        val spraysDeferred = async {
            try {
                apiService.getSprays(language = "en-US").data
            } catch (e: Exception) {
                emptyList()
            }
        }

        cachedTiers = tiersDeferred.await()
        val rawSkins = skinsDeferred.await()
        val rawCards = cardsDeferred.await()
        val rawBuddies = buddiesDeferred.await()
        val rawSprays = spraysDeferred.await()

        cachedRawSkins = rawSkins.associateBy { it.uuid }

        levelToSkinMap = rawSkins.flatMap { skin ->
            skin.levels.map { level -> level.uuid to skin.uuid }
        }.toMap()

        val map = mutableMapOf<String, CatalogItemMeta>()

        // 1. Map Weapon Skins & Skin Levels
        for (skin in rawSkins) {
            val tier = cachedTiers[skin.contentTierUuid] ?: "Select"
            val assetPath = skin.levels.firstOrNull()?.assetPath ?: skin.chromas.firstOrNull()?.assetPath
            val price = getPriceFromTier(tier, skin.displayName, assetPath)
            val skinMeta = CatalogItemMeta(
                uuid = skin.uuid,
                displayName = skin.displayName,
                displayIcon = skin.displayIcon ?: "",
                itemType = if (isMelee(skin.displayName, assetPath)) "Melee" else "Weapon Skin",
                price = price,
                tier = tier
            )
            map[skin.uuid.lowercase()] = skinMeta
            for (lvl in skin.levels) {
                map[lvl.uuid.lowercase()] = skinMeta.copy(uuid = lvl.uuid)
            }
            for (chr in skin.chromas) {
                map[chr.uuid.lowercase()] = skinMeta.copy(
                    uuid = chr.uuid,
                    displayIcon = chr.displayIcon ?: skinMeta.displayIcon
                )
            }
        }

        // 2. Map Player Cards
        for (card in rawCards) {
            val icon = card.largeArt ?: card.wideArt ?: card.displayIcon ?: card.smallArt ?: ""
            val cardMeta = CatalogItemMeta(
                uuid = card.uuid,
                displayName = card.displayName,
                displayIcon = icon,
                itemType = "Player Card",
                price = 375,
                tier = "Select"
            )
            map[card.uuid.lowercase()] = cardMeta
        }

        // 3. Map Gun Buddies
        for (buddy in rawBuddies) {
            val icon = buddy.displayIcon ?: buddy.levels.firstOrNull()?.displayIcon ?: ""
            val buddyMeta = CatalogItemMeta(
                uuid = buddy.uuid,
                displayName = buddy.displayName,
                displayIcon = icon,
                itemType = "Gun Buddy",
                price = 475,
                tier = "Select"
            )
            map[buddy.uuid.lowercase()] = buddyMeta
            for (lvl in buddy.levels) {
                map[lvl.uuid.lowercase()] = buddyMeta.copy(
                    uuid = lvl.uuid,
                    displayIcon = lvl.displayIcon ?: icon
                )
            }
        }

        // 4. Map Sprays
        for (spray in rawSprays) {
            val icon = spray.fullTransparentIcon ?: spray.displayIcon ?: ""
            val sprayMeta = CatalogItemMeta(
                uuid = spray.uuid,
                displayName = spray.displayName,
                displayIcon = icon,
                itemType = "Spray",
                price = 325,
                tier = "Select"
            )
            map[spray.uuid.lowercase()] = sprayMeta
        }

        allCatalogItemMetaMap = map

        cachedSkins = rawSkins
            .filter { it.displayIcon != null }
            .map { skin ->
                val tier = cachedTiers[skin.contentTierUuid] ?: "Select"
                val assetPath = skin.levels.firstOrNull()?.assetPath ?: skin.chromas.firstOrNull()?.assetPath
                val price = getPriceFromTier(tier, skin.displayName, assetPath)
                SkinItem(
                    uuid = skin.uuid,
                    displayName = skin.displayName,
                    displayIcon = skin.displayIcon ?: "",
                    weaponType = if (isMelee(skin.displayName, assetPath)) "Melee" else "Weapon",
                    price = price,
                    discount = 0,
                    tier = tier,
                    skinUuid = skin.uuid
                )
            }
    }

    suspend fun getAllSkins(): List<SkinItem> {
        if (cachedSkins.isEmpty()) {
            ensureAllCatalogMetadataLoaded()
        }
        return cachedSkins
    }

    suspend fun getItemMeta(itemUuid: String): CatalogItemMeta? {
        if (allCatalogItemMetaMap.isEmpty()) {
            ensureAllCatalogMetadataLoaded()
        }
        val key = itemUuid.lowercase()
        val mappedKey = levelToSkinMap[key]?.lowercase() ?: key
        return allCatalogItemMetaMap[key] ?: allCatalogItemMetaMap[mappedKey]
    }

    suspend fun getSkinDetails(skinUuid: String): ValorantSkin? {
        if (cachedRawSkins.containsKey(skinUuid)) {
            return cachedRawSkins[skinUuid]
        }
        return try {
            val response = apiService.getSkinByUuid(skinUuid)
            val skin = response.data
            cachedRawSkins = cachedRawSkins + (skinUuid to skin)
            skin
        } catch (e: Exception) {
            getAllSkins()
            cachedRawSkins[skinUuid]
        }
    }

    fun isMelee(displayName: String, assetPath: String? = null): Boolean {
        if (assetPath?.contains("Melee", ignoreCase = true) == true) return true
        val meleeKeywords = listOf(
            "Knife", "Melee", "Scythe", "Sword", "Dagger", "Blade", "Karambit", "Axe",
            "Kunai", "Baton", "Staff", "Fan", "Claw", "Gauntlet", "Hammer", "Whip",
            "Anchor", "Crescent", "Spellcaster", "Harvester", "Misericórdia", "Lock",
            "Bio-Harvester", "Firefall", "Equilibrium", "Power Fist", "Terminus A-Quo",
            "Mace", "Obsidiana", "Hack", "Sliver", "Cudgel", "Edge", "Boline", "Yaiba",
            "Broken Blade", "Relic Stone", "Onimaru Kunitsuna", "Sparkswitch", "Firefly", "Katana"
        )
        return meleeKeywords.any { displayName.contains(it, ignoreCase = true) }
    }

    fun getPriceFromTier(tier: String, displayName: String = "", assetPath: String? = null): Int {
        val melee = isMelee(displayName, assetPath)

        if (melee) {
            val special5350Keywords = listOf(
                "Nocturnum", "Kuronami", "Evori", "Mystbloom", "Champions", "Ignite",
                "LOCK//IN", "Misericórdia", "Aemondir", "Doombringer", "Imperium",
                "Neo Frontier", "Overdrive", "Singularity", "Holomoku", "Arcane",
                "Onimaru", "Combat Crafts", "VCT"
            )
            if (special5350Keywords.any { displayName.contains(it, ignoreCase = true) }) {
                return 5350
            }

            return when {
                tier.contains("Ultra", ignoreCase = true) -> 4950
                tier.contains("Exclusive", ignoreCase = true) -> 4350
                tier.contains("Premium", ignoreCase = true) -> 3550
                tier.contains("Deluxe", ignoreCase = true) -> 2550
                tier.contains("Select", ignoreCase = true) -> 1750
                else -> 3550
            }
        }

        // Standard Gun pricing
        return when {
            tier.contains("Select", ignoreCase = true) -> 875
            tier.contains("Deluxe", ignoreCase = true) -> 1275
            tier.contains("Premium", ignoreCase = true) -> 1775
            tier.contains("Exclusive", ignoreCase = true) -> 2175
            tier.contains("Ultra", ignoreCase = true) -> 2475
            else -> 1775
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
