package com.example.valomobile.data.repository

import android.util.Base64
import android.util.Log
import com.example.valomobile.data.remote.RiotFriendsApiService
import com.example.valomobile.data.remote.ValorantApiService
import com.example.valomobile.data.remote.model.RiotFriendItemRaw
import com.example.valomobile.data.remote.model.RiotFriendsResponse
import com.example.valomobile.data.remote.model.RiotNameServicePlayer
import com.example.valomobile.data.remote.model.RiotPresenceRawItem
import com.example.valomobile.data.remote.model.RiotPresencesResponse
import com.example.valomobile.data.remote.model.ValorantGamePresencePayload
import com.example.valomobile.data.remote.model.ValorantTierEntry
import com.example.valomobile.domain.model.FriendActivityStatus
import com.example.valomobile.domain.model.FriendItem
import com.example.valomobile.domain.model.FriendsGrouped
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.nio.charset.StandardCharsets
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RiotFriendsRepository @Inject constructor(
    private val authRepository: RiotAuthRepository,
    private val friendsApiService: RiotFriendsApiService,
    private val valorantApiService: ValorantApiService,
    private val gson: Gson
) {
    companion object {
        private const val TAG = "RiotFriendsRepo"
    }

    private var competitiveTiersMap: Map<Int, ValorantTierEntry> = emptyMap()
    private var mapsMap: Map<String, String> = emptyMap()
    private var playerCardsMap: Map<String, String> = emptyMap()
    private var metadataLoaded: Boolean = false

    private suspend fun ensureMetadataLoaded() {
        if (metadataLoaded) return
        withContext(Dispatchers.IO) {
            try {
                // 1. Load Competitive Tiers
                val tierResponse = valorantApiService.getCompetitiveTiers()
                val latestEpisode = tierResponse.data.lastOrNull { it.tiers.isNotEmpty() }
                if (latestEpisode != null) {
                    competitiveTiersMap = latestEpisode.tiers.associateBy { it.tier }
                }

                // 2. Load Maps
                val mapsResponse = valorantApiService.getMaps()
                val tempMaps = mutableMapOf<String, String>()
                mapsResponse.data.forEach { mapItem ->
                    tempMaps[mapItem.displayName.lowercase()] = mapItem.displayName
                    mapItem.mapUrl?.let { url -> tempMaps[url.lowercase()] = mapItem.displayName }
                }
                mapsMap = tempMaps

                // 3. Load Player Cards
                val cardsResponse = valorantApiService.getPlayerCards()
                playerCardsMap = cardsResponse.data.associate { it.uuid.lowercase() to (it.smallArt ?: it.displayIcon ?: "") }

                metadataLoaded = true
            } catch (e: Exception) {
                Log.w(TAG, "Failed to load metadata from valorant-api.com", e)
            }
        }
    }

    suspend fun getFriendsGrouped(): FriendsGrouped = withContext(Dispatchers.IO) {
        if (!authRepository.isLoggedIn) {
            val refreshed = authRepository.refreshSessionSilently()
            if (!refreshed && !authRepository.isLoggedIn) {
                throw IOException("Session expired. Please reconnect your Riot account.")
            }
        }

        try {
            return@withContext fetchFriendsInternal()
        } catch (e: Exception) {
            val isAuthError = e.message?.contains("400") == true
                || e.message?.contains("401") == true
                || e.message?.contains("403") == true
                || e.message?.contains("BAD_CLAIMS") == true
                || e.message?.contains("Unauthorized") == true

            if (isAuthError) {
                Log.w(TAG, "Auth error fetching friends ($e). Attempting silent session refresh...")
                val refreshed = authRepository.refreshSessionSilently()
                if (refreshed) {
                    return@withContext fetchFriendsInternal()
                }
            }
            Log.e(TAG, "Error loading friends from Riot API", e)
            throw e
        }
    }

    private suspend fun fetchFriendsInternal(): FriendsGrouped = withContext(Dispatchers.IO) {
        val accessToken = authRepository.getAccessToken()
            ?: throw IOException("Missing access token.")
        val entitlementsToken = authRepository.getEntitlementsToken()
            ?: throw IOException("Missing entitlements token.")
        val shard = authRepository.getRegion().lowercase()
        val clientVersion = authRepository.getClientVersion()
        val clientPlatform = RiotAuthRepository.CLIENT_PLATFORM
        val authHeader = "Bearer $accessToken"

        ensureMetadataLoaded()

        // 1. Fetch PAS chat token (if available) for Riot Chat services
        var pasChatToken: String? = null
        try {
            val pasRes = friendsApiService.getPasChatToken(authHeader = authHeader)
            if (pasRes.isSuccessful) {
                val pasBody = pasRes.body()?.string()?.trim()
                if (!pasBody.isNullOrBlank()) {
                    // PAS response can be raw JWT string or JSON with token
                    pasChatToken = if (pasBody.startsWith("\"") && pasBody.endsWith("\"")) {
                        pasBody.removeSurrounding("\"")
                    } else if (pasBody.contains("\"token\"")) {
                        val tokenMatch = Regex(""""token"\s*:\s*"([^"]+)"""").find(pasBody)
                        tokenMatch?.groupValues?.get(1) ?: pasBody
                    } else {
                        pasBody
                    }
                    Log.d(TAG, "Successfully acquired PAS Chat token")
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "PAS chat token fetch skipped/failed: ${e.message}")
        }

        val presencesList = mutableListOf<RiotPresenceRawItem>()
        val chatFriendsList = mutableListOf<RiotFriendItemRaw>()

        // 2. Fetch Presences from all candidate endpoints
        val presenceCandidates = listOfNotNull(
            "https://glz-$shard-1.$shard.a.pvp.net/presence/v1/players" to authHeader,
            "https://glz-$shard-1.a.pvp.net/presence/v1/players" to authHeader,
            "https://glz-$shard-1.$shard.a.pvp.net/chat/v4/presences" to authHeader,
            pasChatToken?.let { "https://chat-as.chat.si.riotgames.com/chat/v4/presences" to "Bearer $it" },
            pasChatToken?.let { "https://$shard.chat.si.riotgames.com/chat/v4/presences" to "Bearer $it" }
        )

        for ((url, auth) in presenceCandidates) {
            try {
                Log.d(TAG, "Calling presences endpoint: $url")
                val response = friendsApiService.getRawGet(
                    url = url,
                    authHeader = auth,
                    entitlementsToken = entitlementsToken,
                    clientVersion = clientVersion,
                    clientPlatform = clientPlatform
                )
                if (response.isSuccessful) {
                    val bodyStr = response.body()?.string()
                    if (!bodyStr.isNullOrBlank()) {
                        val presencesObj = try {
                            gson.fromJson(bodyStr, RiotPresencesResponse::class.java)
                        } catch (e: Exception) {
                            null
                        }
                        if (presencesObj?.presences?.isNotEmpty() == true) {
                            presencesList.addAll(presencesObj.presences)
                            Log.d(TAG, "Successfully fetched ${presencesObj.presences.size} presences from $url")
                            break
                        }
                    }
                } else {
                    Log.w(TAG, "Presences URL $url returned HTTP ${response.code()}: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error fetching presences from $url", e)
            }
        }

        // 3. Fetch Friends Roster from all candidate endpoints
        val friendsCandidates = listOfNotNull(
            pasChatToken?.let { "https://chat-as.chat.si.riotgames.com/chat/v4/friends" to "Bearer $it" },
            pasChatToken?.let { "https://$shard.chat.si.riotgames.com/chat/v4/friends" to "Bearer $it" },
            "https://glz-$shard-1.$shard.a.pvp.net/chat/v4/friends" to authHeader,
            "https://pd.$shard.a.pvp.net/chat/v4/friends" to authHeader
        )

        for ((url, auth) in friendsCandidates) {
            try {
                Log.d(TAG, "Calling friends roster endpoint: $url")
                val response = friendsApiService.getRawGet(
                    url = url,
                    authHeader = auth,
                    entitlementsToken = entitlementsToken,
                    clientVersion = clientVersion,
                    clientPlatform = clientPlatform
                )
                if (response.isSuccessful) {
                    val bodyStr = response.body()?.string()
                    if (!bodyStr.isNullOrBlank()) {
                        val friendsObj = try {
                            gson.fromJson(bodyStr, RiotFriendsResponse::class.java)
                        } catch (e: Exception) {
                            null
                        }
                        if (friendsObj?.friends?.isNotEmpty() == true) {
                            chatFriendsList.addAll(friendsObj.friends)
                            Log.d(TAG, "Successfully fetched ${friendsObj.friends.size} friends from $url")
                            break
                        }
                    }
                } else {
                    Log.w(TAG, "Friends URL $url returned HTTP ${response.code()}: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error fetching friends from $url", e)
            }
        }

        val myPuuid = authRepository.getPuuid() ?: ""

        // Collect all distinct friend PUUIDs (excluding our own account)
        val allPuuids = (presencesList.map { it.puuid } + chatFriendsList.map { it.puuid })
            .filter { it.isNotBlank() && it != myPuuid }
            .distinct()

        Log.d(TAG, "Total unique friend PUUIDs collected: ${allPuuids.size}")

        if (allPuuids.isEmpty()) {
            return@withContext FriendsGrouped()
        }

        // 4. Resolve exact Riot IDs (GameName#TagLine) via Name-Service API
        val nameServiceUrl = "https://pd.$shard.a.pvp.net/name-service/v2/players"
        val nameServicePlayers = mutableListOf<RiotNameServicePlayer>()
        try {
            val response = friendsApiService.getRawPut(
                url = nameServiceUrl,
                authHeader = authHeader,
                entitlementsToken = entitlementsToken,
                clientVersion = clientVersion,
                clientPlatform = clientPlatform,
                body = allPuuids
            )
            if (response.isSuccessful) {
                val bodyStr = response.body()?.string()
                if (!bodyStr.isNullOrBlank()) {
                    val type = object : TypeToken<List<RiotNameServicePlayer>>() {}.type
                    val list = gson.fromJson<List<RiotNameServicePlayer>>(bodyStr, type)
                    nameServicePlayers.addAll(list)
                }
            } else {
                Log.w(TAG, "Name-service returned HTTP ${response.code()}: ${response.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to resolve player names from Name-Service", e)
        }

        val namesMap = nameServicePlayers.associateBy { it.subject }
        val presencesMap = presencesList.associateBy { it.puuid }

        val friendsItems = allPuuids.map { puuid ->
            val nameInfo = namesMap[puuid]
            val presence = presencesMap[puuid]

            buildFriendItem(
                puuid = puuid,
                nameInfo = nameInfo,
                presence = presence
            )
        }

        // 5. Group friends by live status
        val inGameList = friendsItems.filter { it.activityStatus == FriendActivityStatus.INGAME || it.activityStatus == FriendActivityStatus.PREGAME }
            .sortedBy { it.gameName.lowercase() }

        val onlineList = friendsItems.filter {
            it.activityStatus == FriendActivityStatus.IN_QUEUE ||
            it.activityStatus == FriendActivityStatus.IN_LOBBY ||
            it.activityStatus == FriendActivityStatus.AWAY ||
            it.activityStatus == FriendActivityStatus.OTHER_GAME
        }.sortedBy { it.gameName.lowercase() }

        val offlineList = friendsItems.filter { it.activityStatus == FriendActivityStatus.OFFLINE }
            .sortedBy { it.gameName.lowercase() }

        return@withContext FriendsGrouped(
            inGame = inGameList,
            online = onlineList,
            offline = offlineList
        )
    }

    private fun buildFriendItem(
        puuid: String,
        nameInfo: RiotNameServicePlayer?,
        presence: RiotPresenceRawItem?
    ): FriendItem {
        val gameName = nameInfo?.gameName ?: presence?.gameName ?: "Valorant Player"
        val tagLine = nameInfo?.tagLine ?: presence?.gameTag ?: ""

        if (presence == null) {
            return FriendItem(
                puuid = puuid,
                gameName = gameName,
                tagLine = tagLine,
                activityStatus = FriendActivityStatus.OFFLINE,
                isOnline = false
            )
        }

        val isValorant = presence.product?.equals("valorant", ignoreCase = true) == true
        val isAway = presence.state?.equals("away", ignoreCase = true) == true || presence.show?.equals("away", ignoreCase = true) == true

        if (!isValorant) {
            return FriendItem(
                puuid = puuid,
                gameName = gameName,
                tagLine = tagLine,
                activityStatus = if (isAway) FriendActivityStatus.AWAY else FriendActivityStatus.OTHER_GAME,
                isOnline = true
            )
        }

        // Decode Base64 Valorant game presence payload
        val payload = decodeGamePresence(presence.privateBase64)

        val loopState = payload?.sessionLoopState?.uppercase() ?: ""
        val partyState = payload?.partyState?.uppercase() ?: ""

        val activityStatus = when {
            loopState == "INGAME" -> FriendActivityStatus.INGAME
            loopState == "PREGAME" -> FriendActivityStatus.PREGAME
            loopState == "MENUS" && partyState == "MATCHMAKING" -> FriendActivityStatus.IN_QUEUE
            loopState == "MENUS" -> if (isAway) FriendActivityStatus.AWAY else FriendActivityStatus.IN_LOBBY
            isAway -> FriendActivityStatus.AWAY
            else -> FriendActivityStatus.IN_LOBBY
        }

        val queueId = payload?.queueId
        val queueDisplayName = formatQueueName(queueId)

        val mapPath = payload?.matchMap
        val mapDisplayName = formatMapName(mapPath)

        val tier = payload?.competitiveTier ?: 0
        val tierEntry = competitiveTiersMap[tier]
        val rankName = tierEntry?.tierName ?: formatDefaultRankName(tier)
        val rankIconUrl = tierEntry?.largeIcon ?: tierEntry?.smallIcon
        val rankColor = tierEntry?.color

        val cardId = payload?.playerCardId?.lowercase()
        val cardSmallUrl = cardId?.let { playerCardsMap[it] }

        return FriendItem(
            puuid = puuid,
            gameName = gameName,
            tagLine = tagLine,
            activityStatus = activityStatus,
            isOnline = true,
            queueId = queueId,
            queueDisplayName = queueDisplayName,
            mapPath = mapPath,
            mapDisplayName = mapDisplayName,
            scoreAlly = if (activityStatus == FriendActivityStatus.INGAME) payload?.partyOwnerMatchScoreAllyTeam else null,
            scoreEnemy = if (activityStatus == FriendActivityStatus.INGAME) payload?.partyOwnerMatchScoreEnemyTeam else null,
            partySize = (payload?.partySize ?: 1).coerceAtLeast(1),
            maxPartySize = (payload?.maxPartySize ?: 5).coerceAtLeast(1),
            accountLevel = payload?.accountLevel ?: 0,
            competitiveTier = tier,
            rankName = rankName,
            rankIconUrl = rankIconUrl,
            rankColor = rankColor,
            playerCardSmallUrl = cardSmallUrl,
            lastOnlineTime = presence.time
        )
    }

    private fun decodeGamePresence(base64Str: String?): ValorantGamePresencePayload? {
        if (base64Str.isNullOrBlank()) return null
        return try {
            val decodedBytes = try {
                java.util.Base64.getDecoder().decode(base64Str)
            } catch (e: Throwable) {
                try {
                    Base64.decode(base64Str, Base64.DEFAULT)
                } catch (e2: Throwable) {
                    null
                }
            } ?: return null
            val jsonString = String(decodedBytes, StandardCharsets.UTF_8)
            gson.fromJson(jsonString, ValorantGamePresencePayload::class.java)
        } catch (e: Exception) {
            null
        }
    }

    private fun formatQueueName(queueId: String?): String? {
        if (queueId.isNullOrBlank()) return null
        return when (queueId.lowercase()) {
            "competitive" -> "Competitive"
            "unrated" -> "Unrated"
            "swiftplay" -> "Swiftplay"
            "spikerush" -> "Spike Rush"
            "deathmatch" -> "Deathmatch"
            "ggteam" -> "Escalation"
            "hurm" -> "Team Deathmatch"
            "premier" -> "Premier"
            "custom" -> "Custom Game"
            "newmap" -> "New Map"
            "snowball" -> "Snowball Fight"
            else -> queueId.replaceFirstChar { it.uppercase() }
        }
    }

    private fun formatMapName(mapPath: String?): String? {
        if (mapPath.isNullOrBlank()) return null

        // Check if cached from valorant-api.com
        mapsMap[mapPath.lowercase()]?.let { return it }

        // Built-in map lookup table
        return when {
            mapPath.contains("Ascent", ignoreCase = true) -> "Ascent"
            mapPath.contains("Bonsai", ignoreCase = true) || mapPath.contains("Split", ignoreCase = true) -> "Split"
            mapPath.contains("Duality", ignoreCase = true) || mapPath.contains("Bind", ignoreCase = true) -> "Bind"
            mapPath.contains("Foxtrot", ignoreCase = true) || mapPath.contains("Breeze", ignoreCase = true) -> "Breeze"
            mapPath.contains("Canyon", ignoreCase = true) || mapPath.contains("Fracture", ignoreCase = true) -> "Fracture"
            mapPath.contains("Triad", ignoreCase = true) || mapPath.contains("Haven", ignoreCase = true) -> "Haven"
            mapPath.contains("Port", ignoreCase = true) || mapPath.contains("Icebox", ignoreCase = true) -> "Icebox"
            mapPath.contains("Pitt", ignoreCase = true) || mapPath.contains("Pearl", ignoreCase = true) -> "Pearl"
            mapPath.contains("Jam", ignoreCase = true) || mapPath.contains("Lotus", ignoreCase = true) -> "Lotus"
            mapPath.contains("Jules", ignoreCase = true) || mapPath.contains("Sunset", ignoreCase = true) -> "Sunset"
            mapPath.contains("Infinity", ignoreCase = true) || mapPath.contains("Abyss", ignoreCase = true) -> "Abyss"
            mapPath.contains("District", ignoreCase = true) -> "District"
            mapPath.contains("Kasbah", ignoreCase = true) -> "Kasbah"
            mapPath.contains("Piazza", ignoreCase = true) -> "Piazza"
            mapPath.contains("Drift", ignoreCase = true) -> "Drift"
            mapPath.contains("Range", ignoreCase = true) || mapPath.contains("Poveglia", ignoreCase = true) -> "The Range"
            else -> mapPath.substringAfterLast("/").substringBefore(".")
        }
    }

    private fun formatDefaultRankName(tier: Int): String {
        return when (tier) {
            0, 1, 2 -> "Unrated"
            3 -> "Iron 1"
            4 -> "Iron 2"
            5 -> "Iron 3"
            6 -> "Bronze 1"
            7 -> "Bronze 2"
            8 -> "Bronze 3"
            9 -> "Silver 1"
            10 -> "Silver 2"
            11 -> "Silver 3"
            12 -> "Gold 1"
            13 -> "Gold 2"
            14 -> "Gold 3"
            15 -> "Platinum 1"
            16 -> "Platinum 2"
            17 -> "Platinum 3"
            18 -> "Diamond 1"
            19 -> "Diamond 2"
            20 -> "Diamond 3"
            21 -> "Ascendant 1"
            22 -> "Ascendant 2"
            23 -> "Ascendant 3"
            24 -> "Immortal 1"
            25 -> "Immortal 2"
            26 -> "Immortal 3"
            27 -> "Radiant"
            else -> "Unrated"
        }
    }
}
