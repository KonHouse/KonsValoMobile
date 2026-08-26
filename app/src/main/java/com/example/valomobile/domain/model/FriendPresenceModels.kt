package com.example.valomobile.domain.model

enum class FriendActivityStatus {
    INGAME,          // 🟢 W grze (np. Competitive na Ascent)
    PREGAME,         // 🟡 Wybór agenta
    IN_QUEUE,        // 🔵 Szuka gry (Matchmaking)
    IN_LOBBY,        // 🟢 W lobby / menu
    AWAY,            // 🟠 Zaraz wracam (Away / AFK)
    OTHER_GAME,      // 🟣 W innej grze Riot (League, TFT, LoR)
    OFFLINE          // ⚪ Niedostępny
}

data class FriendItem(
    val puuid: String,
    val gameName: String,
    val tagLine: String,
    val activityStatus: FriendActivityStatus,
    val isOnline: Boolean,
    val queueId: String? = null,
    val queueDisplayName: String? = null,
    val mapPath: String? = null,
    val mapDisplayName: String? = null,
    val scoreAlly: Int? = null,
    val scoreEnemy: Int? = null,
    val partySize: Int = 1,
    val maxPartySize: Int = 5,
    val accountLevel: Int = 0,
    val competitiveTier: Int = 0,
    val rankName: String = "Unrated",
    val rankIconUrl: String? = null,
    val rankColor: String? = null,
    val playerCardSmallUrl: String? = null,
    val note: String? = null,
    val lastOnlineTime: Long? = null
) {
    val riotId: String
        get() = if (tagLine.isNotBlank()) "$gameName#$tagLine" else gameName

    val matchScoreString: String?
        get() = if (scoreAlly != null && scoreEnemy != null) "$scoreAlly : $scoreEnemy" else null

    val isPartyFull: Boolean
        get() = partySize >= maxPartySize

    val partyString: String
        get() = "$partySize/$maxPartySize"
}

data class FriendsGrouped(
    val inGame: List<FriendItem> = emptyList(),
    val online: List<FriendItem> = emptyList(),
    val offline: List<FriendItem> = emptyList()
) {
    val totalOnlineCount: Int
        get() = inGame.size + online.size

    val totalCount: Int
        get() = inGame.size + online.size + offline.size
}
