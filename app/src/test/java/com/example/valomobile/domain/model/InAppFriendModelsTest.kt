package com.example.valomobile.domain.model

import org.junit.Assert.*
import org.junit.Test
import java.time.ZoneOffset
import java.time.ZonedDateTime

class InAppFriendModelsTest {

    @Test
    fun isSyncedToday_whenUpdatedTodayAfter00Utc_returnsTrue() {
        val nowUtc = ZonedDateTime.now(ZoneOffset.UTC)
        val todaySyncTime = nowUtc.toLocalDate().atStartOfDay(ZoneOffset.UTC).plusHours(2).toInstant().toEpochMilli()

        val friend = InAppFriendItem(
            puuid = "test-puuid-1",
            riotId = "Friend#EUW",
            storeOffers = listOf(
                CloudStoreSkinOffer(uuid = "s1", displayName = "Prime Vandal", price = 1775)
            ),
            lastUpdated = todaySyncTime
        )

        assertTrue(friend.isSyncedToday)
    }

    @Test
    fun isSyncedToday_whenUpdatedYesterdayBefore00Utc_returnsFalse() {
        val nowUtc = ZonedDateTime.now(ZoneOffset.UTC)
        val yesterdaySyncTime = nowUtc.toLocalDate().minusDays(1).atStartOfDay(ZoneOffset.UTC).plusHours(14).toInstant().toEpochMilli()

        val friend = InAppFriendItem(
            puuid = "test-puuid-2",
            riotId = "Friend#EUW",
            storeOffers = listOf(
                CloudStoreSkinOffer(uuid = "s1", displayName = "Reaver Vandal", price = 1775)
            ),
            lastUpdated = yesterdaySyncTime
        )

        assertFalse(friend.isSyncedToday)
    }

    @Test
    fun isSyncedToday_whenStoreOffersEmpty_returnsFalse() {
        val friend = InAppFriendItem(
            puuid = "test-puuid-3",
            riotId = "Friend#EUW",
            storeOffers = emptyList(),
            lastUpdated = System.currentTimeMillis()
        )

        assertFalse(friend.isSyncedToday)
    }
}
