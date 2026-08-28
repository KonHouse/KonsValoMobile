package com.example.valomobile.domain.model

import java.time.ZoneOffset
import java.time.ZonedDateTime

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
) {
    /**
     * Checks if the friend's daily store was synchronized during the current Valorant store cycle.
     * Riot resets daily stores globally at 00:00:00 UTC.
     */
    val isSyncedToday: Boolean
        get() {
            if (storeOffers.isEmpty() || lastUpdated <= 0L) return false
            val startOfCycleMs = getStartOfCurrentStoreCycleMs()
            return lastUpdated >= startOfCycleMs
        }

    companion object {
        fun getStartOfCurrentStoreCycleMs(): Long {
            return try {
                val nowUtc = ZonedDateTime.now(ZoneOffset.UTC)
                val startOfCycleUtc = nowUtc.toLocalDate().atStartOfDay(ZoneOffset.UTC)
                startOfCycleUtc.toInstant().toEpochMilli()
            } catch (e: Exception) {
                // Fallback to 24 hours ago if date calculation fails
                System.currentTimeMillis() - 24 * 60 * 60 * 1000L
            }
        }
    }
}
