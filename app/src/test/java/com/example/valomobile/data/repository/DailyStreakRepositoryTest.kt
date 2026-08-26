package com.example.valomobile.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.valomobile.data.local.StoreHistoryDao
import com.example.valomobile.data.local.StoreHistoryEntity
import com.example.valomobile.domain.model.SkinItem
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.*
import java.time.LocalDate

class DailyStreakRepositoryTest {

    private lateinit var context: Context
    private val memoryStore = mutableMapOf<String, Any>()
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var fakeStoreHistoryDao: FakeStoreHistoryDao
    private val gson = Gson()
    private lateinit var repository: DailyStreakRepository

    class FakeStoreHistoryDao : StoreHistoryDao {
        private val records = mutableMapOf<String, StoreHistoryEntity>()

        override suspend fun insertOrUpdateStoreHistory(history: StoreHistoryEntity) {
            records["${history.date}_${history.puuid}"] = history
        }

        override suspend fun getStoreHistoryForDate(date: String, puuid: String): StoreHistoryEntity? {
            return records["${date}_${puuid}"]
        }

        override fun getAllStoreHistory(puuid: String): Flow<List<StoreHistoryEntity>> {
            return flowOf(records.values.filter { it.puuid == puuid })
        }

        override fun getRecordedDates(puuid: String): Flow<List<String>> {
            return flowOf(records.values.filter { it.puuid == puuid }.map { it.date })
        }

        override suspend fun getRecordedCount(puuid: String): Int {
            return records.values.count { it.puuid == puuid }
        }
    }

    @Before
    fun setUp() {
        memoryStore.clear()
        context = mock(Context::class.java)
        sharedPreferences = mock(SharedPreferences::class.java)
        editor = mock(SharedPreferences.Editor::class.java)
        fakeStoreHistoryDao = FakeStoreHistoryDao()

        `when`(context.getSharedPreferences(anyString(), anyInt())).thenReturn(sharedPreferences)
        `when`(sharedPreferences.edit()).thenReturn(editor)

        `when`(editor.putInt(anyString(), anyInt())).thenAnswer { invocation ->
            val key = invocation.getArgument<String>(0)
            val value = invocation.getArgument<Int>(1)
            memoryStore[key] = value
            editor
        }

        `when`(editor.putString(anyString(), anyString())).thenAnswer { invocation ->
            val key = invocation.getArgument<String>(0)
            val value = invocation.getArgument<String>(1)
            memoryStore[key] = value
            editor
        }

        `when`(editor.putStringSet(anyString(), any())).thenAnswer { invocation ->
            val key = invocation.getArgument<String>(0)
            val value = invocation.getArgument<Set<String>>(1)
            memoryStore[key] = value
            editor
        }

        `when`(sharedPreferences.getInt(anyString(), anyInt())).thenAnswer { invocation ->
            val key = invocation.getArgument<String>(0)
            val def = invocation.getArgument<Int>(1)
            (memoryStore[key] as? Int) ?: def
        }

        `when`(sharedPreferences.getString(anyString(), any())).thenAnswer { invocation ->
            val key = invocation.getArgument<String>(0)
            val def = invocation.getArgument<String?>(1)
            (memoryStore[key] as? String) ?: def
        }

        @Suppress("UNCHECKED_CAST")
        `when`(sharedPreferences.getStringSet(anyString(), any())).thenAnswer { invocation ->
            val key = invocation.getArgument<String>(0)
            val def = invocation.getArgument<Set<String>?>(1)
            (memoryStore[key] as? Set<String>) ?: def ?: emptySet<String>()
        }

        repository = DailyStreakRepository(context, fakeStoreHistoryDao, gson)
    }

    @Test
    fun recordDailyVisit_firstLaunch_startsStreakAtOne() {
        val date1 = LocalDate.of(2026, 8, 20)
        val result = repository.recordDailyVisit(date1)

        assertTrue(result.isNewDay)
        assertTrue(result.isStreakIncreased)
        assertFalse(result.wasBroken)
        assertEquals(1, result.currentStreak)
        assertEquals(1, result.maxStreak)
    }

    @Test
    fun recordDailyVisit_sameDayMultipleVisits_doesNotIncrement() {
        val date1 = LocalDate.of(2026, 8, 20)
        repository.recordDailyVisit(date1)

        val secondResult = repository.recordDailyVisit(date1)
        assertFalse(secondResult.isNewDay)
        assertFalse(secondResult.isStreakIncreased)
        assertEquals(1, secondResult.currentStreak)
        assertEquals(1, secondResult.maxStreak)
    }

    @Test
    fun recordDailyVisit_consecutiveDays_incrementsStreak() {
        val date1 = LocalDate.of(2026, 8, 20)
        val date2 = LocalDate.of(2026, 8, 21)
        val date3 = LocalDate.of(2026, 8, 22)

        repository.recordDailyVisit(date1)
        val res2 = repository.recordDailyVisit(date2)
        val res3 = repository.recordDailyVisit(date3)

        assertTrue(res2.isStreakIncreased)
        assertEquals(2, res2.currentStreak)
        assertEquals(2, res2.maxStreak)

        assertTrue(res3.isStreakIncreased)
        assertEquals(3, res3.currentStreak)
        assertEquals(3, res3.maxStreak)
    }

    @Test
    fun recordDailyVisit_missedDays_resetsStreakAndRetainsMax() {
        val date1 = LocalDate.of(2026, 8, 20)
        val date2 = LocalDate.of(2026, 8, 21)
        val dateMissed = LocalDate.of(2026, 8, 24) // 3 days later

        repository.recordDailyVisit(date1)
        repository.recordDailyVisit(date2)

        val brokenResult = repository.recordDailyVisit(dateMissed)
        assertTrue(brokenResult.isNewDay)
        assertTrue(brokenResult.wasBroken)
        assertEquals(1, brokenResult.currentStreak)
        assertEquals(2, brokenResult.maxStreak) // Max streak preserved!
    }

    @Test
    fun getStreakInfo_populatesWeekDays() {
        val today = LocalDate.of(2026, 8, 26) // Wednesday
        repository.recordDailyVisit(today)

        val info = repository.getStreakInfo(today)
        assertEquals(1, info.currentStreak)
        assertTrue(info.isCheckedInToday)
        assertEquals(7, info.weekDays.size)

        val todayStatus = info.weekDays.first { it.isToday }
        assertTrue(todayStatus.isCompleted)
    }

    @Test
    fun saveAndGetStoreHistory_persistsCorrectly() = runBlocking {
        val date = LocalDate.of(2026, 8, 26)
        val skins = listOf(
            SkinItem(
                uuid = "skin-1",
                displayName = "Reaver Vandal",
                displayIcon = "icon.png",
                weaponType = "Vandal",
                price = 1775,
                discount = 0,
                tier = "Premium",
                skinUuid = "skin-1"
            )
        )

        repository.saveTodayStore(skins, "puuid-123", date)

        val loaded = repository.getStoreHistoryForDate(date, "puuid-123")
        assertNotNull(loaded)
        assertEquals(1, loaded!!.size)
        assertEquals("Reaver Vandal", loaded[0].displayName)
    }

    @Test
    fun recordDailyVisit_multiAccount_isolatedStreaks() {
        val date1 = LocalDate.of(2026, 8, 20)
        val date2 = LocalDate.of(2026, 8, 21)

        // Account A logs in for 2 consecutive days
        repository.recordDailyVisit(today = date1, puuid = "account-A")
        val resA = repository.recordDailyVisit(today = date2, puuid = "account-A")
        assertEquals(2, resA.currentStreak)

        // Account B logs in for the first time on date2
        val resB = repository.recordDailyVisit(today = date2, puuid = "account-B")
        assertEquals(1, resB.currentStreak)

        // Verify account A still has streak 2
        val infoA = repository.getStreakInfo(today = date2, puuid = "account-A")
        assertEquals(2, infoA.currentStreak)
    }
}
