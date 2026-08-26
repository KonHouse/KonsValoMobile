package com.example.valomobile.domain.model

data class WeekDayStatus(
    val dayName: String,
    val dayOfMonth: Int,
    val isToday: Boolean,
    val isCompleted: Boolean,
    val isPast: Boolean
)

data class DailyStreakInfo(
    val currentStreak: Int = 0,
    val maxStreak: Int = 0,
    val lastLoginDate: String? = null,
    val isCheckedInToday: Boolean = false,
    val weekDays: List<WeekDayStatus> = emptyList()
)

data class StreakCheckInResult(
    val currentStreak: Int,
    val maxStreak: Int,
    val isNewDay: Boolean,
    val isStreakIncreased: Boolean,
    val wasBroken: Boolean
)
