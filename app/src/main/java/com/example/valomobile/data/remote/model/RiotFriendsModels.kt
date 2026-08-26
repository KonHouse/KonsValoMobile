package com.example.valomobile.data.remote.model

import com.google.gson.annotations.SerializedName

data class RiotPresencesResponse(
    @SerializedName("presences")
    val presences: List<RiotPresenceRawItem> = emptyList()
)

data class RiotPresenceRawItem(
    @SerializedName("puuid")
    val puuid: String,
    @SerializedName("game_name")
    val gameName: String? = null,
    @SerializedName("game_tag")
    val gameTag: String? = null,
    @SerializedName("product")
    val product: String? = null,
    @SerializedName("state")
    val state: String? = null,
    @SerializedName("show")
    val show: String? = null,
    @SerializedName("private")
    val privateBase64: String? = null,
    @SerializedName("time")
    val time: Long? = null
)

data class ValorantGamePresencePayload(
    val isValid: Boolean = true,
    val sessionLoopState: String? = null, // "MENUS", "PREGAME", "INGAME"
    val partyState: String? = null, // "DEFAULT", "MATCHMAKING", "INGAME"
    val partySize: Int = 1,
    val maxPartySize: Int = 5,
    val partyAccessibility: String? = null,
    val matchMap: String? = null,
    val queueId: String? = null,
    val partyOwnerMatchScoreAllyTeam: Int = 0,
    val partyOwnerMatchScoreEnemyTeam: Int = 0,
    val accountLevel: Int = 0,
    val competitiveTier: Int = 0,
    val leaderboardPosition: Int = 0,
    val playerCardId: String? = null,
    val playerTitleId: String? = null
)

data class RiotNameServicePlayer(
    @SerializedName("DisplayName")
    val displayName: String? = null,
    @SerializedName("Subject")
    val subject: String,
    @SerializedName("GameName")
    val gameName: String? = null,
    @SerializedName("TagLine")
    val tagLine: String? = null
)

data class RiotFriendsResponse(
    @SerializedName("friends")
    val friends: List<RiotFriendItemRaw> = emptyList()
)

data class RiotFriendItemRaw(
    @SerializedName("puuid")
    val puuid: String,
    @SerializedName("game_name")
    val gameName: String? = null,
    @SerializedName("game_tag")
    val gameTag: String? = null,
    @SerializedName("name")
    val name: String? = null,
    @SerializedName("note")
    val note: String? = null
)
