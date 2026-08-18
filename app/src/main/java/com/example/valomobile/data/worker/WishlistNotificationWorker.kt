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
import com.example.valomobile.data.local.WishlistDao
import com.example.valomobile.data.repository.RiotStoreRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class WishlistNotificationWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: RiotStoreRepository,
    private val wishlistDao: WishlistDao
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        Log.d("WishlistWorker", "WishlistNotificationWorker started")

        val prefs = applicationContext.getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)
        val notificationsEnabled = prefs.getBoolean("wishlist_notifications", true)

        if (!notificationsEnabled) {
            Log.d("WishlistWorker", "Notifications are disabled in settings. Skipping.")
            return@withContext Result.success()
        }

        try {
            val wishlist = wishlistDao.getAllWishlistItemsSync()
            Log.d("WishlistWorker", "Fetched ${wishlist.size} items from wishlist")
            if (wishlist.isEmpty()) {
                Log.d("WishlistWorker", "Wishlist is empty. Nothing to check.")
                return@withContext Result.success()
            }

            val storeRotation = repository.getStoreRotation()
            Log.d("WishlistWorker", "Fetched ${storeRotation.size} items from store rotation")

            val nightMarket = repository.getNightMarket()
            Log.d("WishlistWorker", "Fetched ${nightMarket.size} items from night market")

            val allAvailableSkins = storeRotation + nightMarket
            val matches = wishlist.filter { wishItem ->
                val match = allAvailableSkins.any { availableItem -> availableItem.uuid == wishItem.uuid }
                if (match) {
                    Log.d("WishlistWorker", "Match found: ${wishItem.name} (${wishItem.uuid})")
                }
                match
            }

            if (matches.isNotEmpty()) {
                Log.d("WishlistWorker", "Found ${matches.size} matches. Triggering notification.")
                showNotification(matches.size, matches.first().name)
            } else {
                Log.d("WishlistWorker", "No matches found between wishlist and current store.")
            }

            Result.success()
        } catch (e: Exception) {
            Log.e("WishlistWorker", "Error in WishlistNotificationWorker", e)
            Result.retry()
        }
    }

    private fun showNotification(count: Int, firstName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    applicationContext,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.w("WishlistWorker", "POST_NOTIFICATIONS permission not granted. Skipping notification.")
                return
            }
        }

        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "wishlist_alerts"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Wishlist Alerts",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications when a wishlisted skin is in the store"
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
            PendingIntent.FLAG_IMMUTABLE
        )

        val title = if (count == 1) "Skin Available!" else "$count Skins Available!"
        val text = if (count == 1) "$firstName is now in your store!" else "$firstName and ${count - 1} more are in your store!"

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Using system icon for now
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1, notification)
    }
}
