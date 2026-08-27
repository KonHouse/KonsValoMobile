package com.example.valomobile.data.worker

import android.content.Context
import android.util.Log
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

object DailyReminderScheduler {
    private const val TAG = "DailyReminderScheduler"
    const val WORK_NAME = "daily_shop_reminder_work"
    const val PREFS_NAME = "settings_prefs"
    const val KEY_DAILY_REMINDER_ENABLED = "daily_reminder_enabled"
    const val KEY_DAILY_REMINDER_HOUR = "daily_reminder_hour"
    const val KEY_DAILY_REMINDER_MINUTE = "daily_reminder_minute"

    fun schedule(context: Context, hour: Int, minute: Int, isEnabled: Boolean) {
        val workManager = WorkManager.getInstance(context)
        if (!isEnabled) {
            Log.d(TAG, "Daily reminder disabled. Canceling scheduled work.")
            workManager.cancelUniqueWork(WORK_NAME)
            return
        }

        val now = LocalDateTime.now()
        var target = now.withHour(hour).withMinute(minute).withSecond(0).withNano(0)
        if (target.isBefore(now) || target.isEqual(now)) {
            target = target.plusDays(1)
        }
        val delayMs = Duration.between(now, target).toMillis().coerceAtLeast(0L)

        Log.d(TAG, "Scheduling daily reminder for $hour:${minute.toString().padStart(2, '0')} (in ${delayMs / 1000 / 60} minutes)")

        val request = OneTimeWorkRequestBuilder<DailyReminderWorker>()
            .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
            .addTag(WORK_NAME)
            .build()

        workManager.enqueueUniqueWork(
            WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    fun scheduleNext(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val isEnabled = prefs.getBoolean(KEY_DAILY_REMINDER_ENABLED, false)
        val hour = prefs.getInt(KEY_DAILY_REMINDER_HOUR, 18)
        val minute = prefs.getInt(KEY_DAILY_REMINDER_MINUTE, 0)
        if (isEnabled) {
            schedule(context, hour, minute, isEnabled = true)
        }
    }
}
