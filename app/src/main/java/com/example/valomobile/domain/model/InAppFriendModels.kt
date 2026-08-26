package com.example.valomobile.domain.model

enum class InviteStatus {
    PENDING,
    ACCEPTED,
    REJECTED
}

data class CloudStoreSkinOffer(
    val uuid: String = "",
    val displayName: String = "",
    val displayIcon: String? = null,
    val price: Int = 0,
    val tierColor: String? = null,
    val tierIcon: String? = null
)

data class CloudUserProfile(
    val puuid: String = "",
    val friendCode: String = "",
    val riotId: String = "",
    val region: String = "eu",
    val accountLevel: Int = 0,
    val currentStreak: Int = 0,
    val storeOffers: List<CloudStoreSkinOffer> = emptyList(),
    val lastUpdated: Long = System.currentTimeMillis()
)

data class FriendInvite(
    val id: String = "",
    val fromPuuid: String = "",
    val fromRiotId: String = "",
    val fromFriendCode: String = "",
    val toPuuid: String = "",
    val toRiotId: String = "",
    val status: String = InviteStatus.PENDING.name,
    val createdAt: Long = System.currentTimeMillis()
)

data class InAppFriendItem(
    val puuid: String = "",
    val friendCode: String = "",
    val riotId: String = "",
    val region: String = "eu",
    val accountLevel: Int = 0,
    val currentStreak: Int = 0,
    val storeOffers: List<CloudStoreSkinOffer> = emptyList(),
    val lastUpdated: Long = 0L,
    val isOnlineRecently: Boolean = false
)
