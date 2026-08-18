package com.example.valomobile

import android.app.Application
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.*
import com.example.valomobile.data.worker.StoreUpdateWorker
import com.example.valomobile.data.worker.WishlistNotificationWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class ValoApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() {
            Log.d("ValoApplication", "Providing WorkManager configuration. WorkerFactory initialized: ${::workerFactory.isInitialized}")
            return Configuration.Builder()
                .setWorkerFactory(workerFactory)
                .setMinimumLoggingLevel(Log.DEBUG)
                .build()
        }

    override fun onCreate() {
        super.onCreate()
        Log.d("ValoApplication", "Application onCreate. WorkerFactory initialized: ${::workerFactory.isInitialized}")
        scheduleWorkers()
    }

    private fun scheduleWorkers() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        // Wishlist Notification Worker
        val wishlistWorkRequest = PeriodicWorkRequestBuilder<WishlistNotificationWorker>(
            4, TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "wishlist_check",
            ExistingPeriodicWorkPolicy.UPDATE,
            wishlistWorkRequest
        )

        // Store Update Worker
        val storeUpdateWorkRequest = PeriodicWorkRequestBuilder<StoreUpdateWorker>(
            24, TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "store_update",
            ExistingPeriodicWorkPolicy.REPLACE,
            storeUpdateWorkRequest
        )
    }
}
