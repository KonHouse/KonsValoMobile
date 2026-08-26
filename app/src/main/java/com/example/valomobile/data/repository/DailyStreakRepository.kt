package com.example.valomobile.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.valomobile.data.local.StoreHistoryDao
import com.example.valomobile.data.local.StoreHistoryEntity
import com.example.valomobile.domain.model.DailyStreakInfo
import com.example.valomobile.domain.model.SkinItem
import com.example.valomobile.domain.model.StreakCheckInResult
import com.example.valomobile.domain.model.WeekDayStatus
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DailyStreakRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val storeHistoryDao: StoreHistoryDao,
    private val gson: Gson
) {
    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private val _streakInfo = MutableStateFlow(DailyStreakInfo())
    val streakInfo: StateFlow<DailyStreakInfo> = _streakInfo.asStateFlow()

    private fun getPrefKey(base: String, puuid: String): String {
        return if (puuid.isNotBlank()) "${puuid}_$base" else base
    }

    fun refreshStreakForAccount(puuid: String, today: LocalDate = LocalDate.now()) {
        _streakInfo.value = loadStreakInfo(today, puuid)
    }

    fun recordDailyVisit(today: LocalDate = LocalDate.now(), puuid: String = ""): StreakCheckInResult {
        val todayStr = today.toString()
        val lastDateKey = getPrefKey(KEY_LAST_LOGIN_DATE, puuid)
        val currentStreakKey = getPrefKey(KEY_CURRENT_STREAK, puuid)
        val maxStreakKey = getPrefKey(KEY_MAX_STREAK, puuid)
        val visitedDatesKey = getPrefKey(KEY_VISITED_DATES, puuid)

        val lastDateStr = prefs.getString(lastDateKey, null)
        val currentStreak = prefs.getInt(currentStreakKey, 0)
        val maxStreak = prefs.getInt(maxStreakKey, 0)
        val visitedDates = prefs.getStringSet(visitedDatesKey, emptySet())?.toMutableSet() ?: mutableSetOf()

        if (lastDateStr == todayStr) {
            val info = loadStreakInfo(today, puuid)
            _streakInfo.value = info
            return StreakCheckInResult(
                currentStreak = currentStreak,
                maxStreak = maxStreak,
                isNewDay = false,
                isStreakIncreased = false,
                wasBroken = false
            )
        }

        var newStreak = 1
        var wasBroken = false

        if (lastDateStr != null) {
            try {
                val lastDate = LocalDate.parse(lastDateStr)
                val daysBetween = ChronoUnit.DAYS.between(lastDate, today)

                when {
                    daysBetween == 1L -> {
                        // Consecutive day visit
                        newStreak = currentStreak + 1
                        wasBroken = false
                    }
                    daysBetween > 1L -> {
                        // Missed at least one day
                        newStreak = 1
                        wasBroken = currentStreak > 0
                    }
                    else -> {
                        // Same day or clock skew
                        newStreak = currentStreak.coerceAtLeast(1)
                    }
                }
            } catch (e: Exception) {
                newStreak = 1
            }
        }

        val newMaxStreak = maxOf(maxStreak, newStreak)
        visitedDates.add(todayStr)

        prefs.edit()
            .putInt(currentStreakKey, newStreak)
            .putInt(maxStreakKey, newMaxStreak)
            .putString(lastDateKey, todayStr)
            .putStringSet(visitedDatesKey, visitedDates)
            .apply()

        val updatedInfo = loadStreakInfo(today, puuid)
        _streakInfo.value = updatedInfo

        return StreakCheckInResult(
            currentStreak = newStreak,
            maxStreak = newMaxStreak,
            isNewDay = true,
            isStreakIncreased = true,
            wasBroken = wasBroken
        )
    }

    suspend fun saveTodayStore(skins: List<SkinItem>, puuid: String, date: LocalDate = LocalDate.now()) = withContext(Dispatchers.IO) {
        if (skins.isEmpty() || puuid.isBlank()) return@withContext
        val dateStr = date.toString()
        val json = gson.toJson(skins)
        storeHistoryDao.insertOrUpdateStoreHistory(
            StoreHistoryEntity(
                date = dateStr,
                puuid = puuid,
                itemsJson = json
            )
        )
    }

    suspend fun getStoreHistoryForDate(date: LocalDate, puuid: String): List<SkinItem>? = withContext(Dispatchers.IO) {
        if (puuid.isBlank()) return@withContext null
        val dateStr = date.toString()
        val entity = storeHistoryDao.getStoreHistoryForDate(dateStr, puuid) ?: return@withContext null
        val type = object : TypeToken<List<SkinItem>>() {}.type
        return@withContext try {
            gson.fromJson<List<SkinItem>>(entity.itemsJson, type)
        } catch (e: Exception) {
            null
        }
    }

    fun getRecordedDates(puuid: String): Flow<Set<String>> {
        if (puuid.isBlank()) return kotlinx.coroutines.flow.flowOf(emptySet())
        return storeHistoryDao.getRecordedDates(puuid).map { it.toSet() }
    }

    fun getStreakInfo(today: LocalDate = LocalDate.now(), puuid: String = ""): DailyStreakInfo {
        return loadStreakInfo(today, puuid)
    }

    private fun loadStreakInfo(today: LocalDate = LocalDate.now(), puuid: String = ""): DailyStreakInfo {
        val todayStr = today.toString()
        val lastDateKey = getPrefKey(KEY_LAST_LOGIN_DATE, puuid)
        val currentStreakKey = getPrefKey(KEY_CURRENT_STREAK, puuid)
        val maxStreakKey = getPrefKey(KEY_MAX_STREAK, puuid)
        val visitedDatesKey = getPrefKey(KEY_VISITED_DATES, puuid)

        val currentStreak = prefs.getInt(currentStreakKey, 0)
        val maxStreak = prefs.getInt(maxStreakKey, 0)
        val lastDateStr = prefs.getString(lastDateKey, null)
        val visitedDates = prefs.getStringSet(visitedDatesKey, emptySet()) ?: emptySet()

        // Check if streak was broken due to inactivity
        val isCheckedInToday = lastDateStr == todayStr
        val effectiveStreak = if (lastDateStr != null && !isCheckedInToday) {
            try {
                val lastDate = LocalDate.parse(lastDateStr)
                val daysBetween = ChronoUnit.DAYS.between(lastDate, today)
                if (daysBetween > 1L) 0 else currentStreak
            } catch (e: Exception) {
                currentStreak
            }
        } else {
            currentStreak
        }

        // Build current week status (Mon -> Sun)
        val monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val weekDays = mutableListOf<WeekDayStatus>()

        for (i in 0L..6L) {
            val date = monday.plusDays(i)
            val dateStr = date.toString()
            val shortDayName = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())

            weekDays.add(
                WeekDayStatus(
                    dayName = shortDayName,
                    dayOfMonth = date.dayOfMonth,
                    isToday = date == today,
                    isCompleted = visitedDates.contains(dateStr),
                    isPast = date.isBefore(today)
                )
            )
        }

        return DailyStreakInfo(
            currentStreak = effectiveStreak,
            maxStreak = maxStreak,
            lastLoginDate = lastDateStr,
            isCheckedInToday = isCheckedInToday,
            weekDays = weekDays
        )
    }

    companion object {
        private const val PREFS_NAME = "daily_streak_prefs"
        private const val KEY_CURRENT_STREAK = "current_streak"
        private const val KEY_MAX_STREAK = "max_streak"
        private const val KEY_LAST_LOGIN_DATE = "last_login_date"
        private const val KEY_VISITED_DATES = "visited_dates_set"
    }
}
