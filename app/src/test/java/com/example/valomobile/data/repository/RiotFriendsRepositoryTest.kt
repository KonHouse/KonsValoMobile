package com.example.valomobile.data.repository

import com.example.valomobile.data.remote.RiotFriendsApiService
import com.example.valomobile.data.remote.ValorantApiService
import com.example.valomobile.data.remote.model.*
import com.example.valomobile.domain.model.FriendActivityStatus
import com.google.gson.Gson
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*
import retrofit2.Response
import java.nio.charset.StandardCharsets
import java.util.Base64

class RiotFriendsRepositoryTest {

    private lateinit var authRepository: RiotAuthRepository
    private lateinit var friendsApiService: RiotFriendsApiService
    private lateinit var valorantApiService: ValorantApiService
    private val gson = Gson()
    private lateinit var repository: RiotFriendsRepository

    @Before
    fun setUp() {
        authRepository = mock(RiotAuthRepository::class.java)
        friendsApiService = mock(RiotFriendsApiService::class.java)
        valorantApiService = mock(ValorantApiService::class.java)

        `when`(authRepository.isLoggedIn).thenReturn(true)
        `when`(authRepository.getAccessToken()).thenReturn("mock_token")
        `when`(authRepository.getEntitlementsToken()).thenReturn("mock_entitlements")
        `when`(authRepository.getRegion()).thenReturn("eu")
        `when`(authRepository.getClientVersion()).thenReturn("release-09.04")
        `when`(authRepository.getPuuid()).thenReturn("my-puuid")

        repository = RiotFriendsRepository(authRepository, friendsApiService, valorantApiService, gson)
    }

    @Test
    fun getFriendsGrouped_inGameFriend_decodedAndGroupedCorrectly() = runBlocking {
        // Prepare metadata mocks
        `when`(valorantApiService.getCompetitiveTiers("en-US")).thenReturn(
            ValorantApiResponse(
                status = 200,
                data = listOf(
                    ValorantCompetitiveTierEpisode(
                        uuid = "ep-1",
                        assetObjectName = "Episode 9",
                        tiers = listOf(
                            ValorantTierEntry(
                                tier = 18,
                                tierName = "Diamond 1",
                                division = "DIAMOND",
                                divisionName = "Diamond",
                                color = "#a65ee4",
                                backgroundColor = "#5b248a",
                                smallIcon = "https://icon-small.png",
                                largeIcon = "https://icon-large.png",
                                rankTriangle = null
                            )
                        )
                    )
                )
            )
        )
        `when`(valorantApiService.getMaps("en-US")).thenReturn(
            ValorantApiResponse(
                status = 200,
                data = listOf(
                    ValorantMap(
                        uuid = "map-1",
                        displayName = "Ascent",
                        mapUrl = "/Game/Maps/Ascent/Ascent",
                        displayIcon = "icon.png",
                        splash = "splash.png",
                        listViewIcon = null
                    )
                )
            )
        )
        `when`(valorantApiService.getPlayerCards("en-US")).thenReturn(
            ValorantApiResponse(status = 200, data = emptyList())
        )

        // Mock PAS token failure/skip
        `when`(friendsApiService.getPasChatToken(anyString(), anyString())).thenReturn(
            Response.error(404, "".toResponseBody("application/json".toMediaTypeOrNull()))
        )

        // Prepare Base64 payload for In-Game Valorant Friend
        val gamePayload = ValorantGamePresencePayload(
            isValid = true,
            sessionLoopState = "INGAME",
            partyState = "INGAME",
            partySize = 2,
            maxPartySize = 5,
            matchMap = "/Game/Maps/Ascent/Ascent",
            queueId = "competitive",
            partyOwnerMatchScoreAllyTeam = 8,
            partyOwnerMatchScoreEnemyTeam = 6,
            accountLevel = 150,
            competitiveTier = 18
        )
        val jsonPayload = gson.toJson(gamePayload)
        val base64Payload = Base64.getEncoder().encodeToString(jsonPayload.toByteArray(StandardCharsets.UTF_8))

        val presencesResponse = RiotPresencesResponse(
            presences = listOf(
                RiotPresenceRawItem(
                    puuid = "friend-puuid-1",
                    gameName = "AcePlayer",
                    gameTag = "EUW",
                    product = "valorant",
                    state = "chat",
                    privateBase64 = base64Payload,
                    time = System.currentTimeMillis()
                )
            )
        )

        val presencesJson = gson.toJson(presencesResponse)
        `when`(friendsApiService.getRawGet(contains("presence"), anyString(), anyString(), anyString(), anyString()))
            .thenReturn(Response.success(presencesJson.toResponseBody("application/json".toMediaTypeOrNull())))

        val namesResponse = listOf(
            RiotNameServicePlayer(
                displayName = "AcePlayer#EUW",
                subject = "friend-puuid-1",
                gameName = "AcePlayer",
                tagLine = "EUW"
            )
        )
        val namesJson = gson.toJson(namesResponse)
        `when`(friendsApiService.getRawPut(contains("name-service"), anyString(), anyString(), anyString(), anyString(), anyList()))
            .thenReturn(Response.success(namesJson.toResponseBody("application/json".toMediaTypeOrNull())))

        val result = repository.getFriendsGrouped()

        assertEquals(1, result.inGame.size)
        assertEquals(0, result.online.size)
        assertEquals(0, result.offline.size)

        val friend = result.inGame[0]
        assertEquals("friend-puuid-1", friend.puuid)
        assertEquals("AcePlayer#EUW", friend.riotId)
        assertEquals(FriendActivityStatus.INGAME, friend.activityStatus)
        assertEquals("Competitive", friend.queueDisplayName)
        assertEquals("Ascent", friend.mapDisplayName)
        assertEquals(8, friend.scoreAlly)
        assertEquals(6, friend.scoreEnemy)
        assertEquals("8 : 6", friend.matchScoreString)
        assertEquals(150, friend.accountLevel)
        assertEquals(18, friend.competitiveTier)
        assertEquals("Diamond 1", friend.rankName)
        assertEquals("https://icon-large.png", friend.rankIconUrl)
    }

    @Test
    fun getFriendsGrouped_inLobbyAndQueue_categorizedCorrectly() = runBlocking {
        `when`(valorantApiService.getCompetitiveTiers("en-US")).thenReturn(
            ValorantApiResponse(status = 200, data = emptyList())
        )
        `when`(valorantApiService.getMaps("en-US")).thenReturn(
            ValorantApiResponse(status = 200, data = emptyList())
        )
        `when`(valorantApiService.getPlayerCards("en-US")).thenReturn(
            ValorantApiResponse(status = 200, data = emptyList())
        )

        `when`(friendsApiService.getPasChatToken(anyString(), anyString())).thenReturn(
            Response.error(404, "".toResponseBody("application/json".toMediaTypeOrNull()))
        )

        val queuePayload = ValorantGamePresencePayload(
            isValid = true,
            sessionLoopState = "MENUS",
            partyState = "MATCHMAKING",
            queueId = "swiftplay"
        )
        val base64Queue = Base64.getEncoder().encodeToString(gson.toJson(queuePayload).toByteArray(StandardCharsets.UTF_8))

        val presencesResponse = RiotPresencesResponse(
            presences = listOf(
                RiotPresenceRawItem(
                    puuid = "friend-puuid-2",
                    gameName = "LobbyGamer",
                    gameTag = "PL1",
                    product = "valorant",
                    state = "chat",
                    privateBase64 = base64Queue
                )
            )
        )

        val presencesJson = gson.toJson(presencesResponse)
        `when`(friendsApiService.getRawGet(contains("presence"), anyString(), anyString(), anyString(), anyString()))
            .thenReturn(Response.success(presencesJson.toResponseBody("application/json".toMediaTypeOrNull())))

        val namesResponse = listOf(
            RiotNameServicePlayer(
                displayName = "LobbyGamer#PL1",
                subject = "friend-puuid-2",
                gameName = "LobbyGamer",
                tagLine = "PL1"
            )
        )
        val namesJson = gson.toJson(namesResponse)
        `when`(friendsApiService.getRawPut(contains("name-service"), anyString(), anyString(), anyString(), anyString(), anyList()))
            .thenReturn(Response.success(namesJson.toResponseBody("application/json".toMediaTypeOrNull())))

        val result = repository.getFriendsGrouped()

        assertEquals(0, result.inGame.size)
        assertEquals(1, result.online.size)
        val onlineFriend = result.online[0]
        assertEquals(FriendActivityStatus.IN_QUEUE, onlineFriend.activityStatus)
        assertEquals("Swiftplay", onlineFriend.queueDisplayName)
    }
}
