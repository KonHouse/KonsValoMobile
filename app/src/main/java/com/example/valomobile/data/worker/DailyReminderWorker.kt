package com.example.valomobile.data.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.valomobile.MainActivity
import com.example.valomobile.R
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class DailyReminderWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        private const val TAG = "DailyReminderWorker"
        const val CHANNEL_ID = "daily_shop_reminders"
        const val NOTIFICATION_ID = 2001
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        Log.d(TAG, "DailyReminderWorker triggered")

        val prefs = applicationContext.getSharedPreferences(
            DailyReminderScheduler.PREFS_NAME,
            Context.MODE_PRIVATE
        )
        val isEnabled = prefs.getBoolean(
            DailyReminderScheduler.KEY_DAILY_REMINDER_ENABLED,
            false
        )

        if (isEnabled) {
            showReminderNotification()
            // Schedule the reminder for the next day
            DailyReminderScheduler.scheduleNext(applicationContext)
        } else {
            Log.d(TAG, "Daily reminder is disabled in settings. Skipping.")
        }

        Result.success()
    }

    private fun showReminderNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    applicationContext,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.w(TAG, "POST_NOTIFICATIONS permission not granted. Cannot post notification.")
                return
            }
        }

        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Daily Store Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Daily reminders to check your weapon store offers and streak"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("🛒 Your Valorant Shop is Ready!")
            .setContentText("Check today's 4 new weapon skins and keep your daily streak alive! 🔥")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Check today's 4 new weapon skins and keep your daily streak alive! Tap to view your store. 🔥")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
        Log.d(TAG, "Daily reminder notification displayed successfully.")
    }
}
