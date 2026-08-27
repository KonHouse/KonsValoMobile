package com.example.valomobile.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.valomobile.BuildConfig
import com.example.valomobile.data.remote.GitHubApiService
import com.example.valomobile.domain.model.AppUpdateInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppUpdateRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gitHubApiService: GitHubApiService
) {
    companion object {
        private const val TAG = "AppUpdateRepo"
        private const val PREFS_NAME = "app_update_prefs"
        private const val KEY_DISMISSED_VERSION = "dismissed_version"
    }

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    suspend fun checkForUpdate(
        currentVersion: String = BuildConfig.VERSION_NAME,
        ignoreDismissed: Boolean = false
    ): Result<AppUpdateInfo?> = withContext(Dispatchers.IO) {
        try {
            val response = gitHubApiService.getLatestRelease()
            if (!response.isSuccessful || response.body() == null) {
                Log.d(TAG, "No GitHub release found or request failed: ${response.code()}")
                return@withContext Result.success(null)
            }

            val release = response.body()!!
            val rawLatestTag = release.tagName.trim()
            val cleanLatestVersion = rawLatestTag.removePrefix("v").removePrefix("V").trim()
            val cleanCurrentVersion = currentVersion.removePrefix("v").removePrefix("V").trim()

            val isNewer = isNewerVersion(cleanLatestVersion, cleanCurrentVersion)

            if (!isNewer) {
                Log.d(TAG, "Current version ($currentVersion) is up to date with latest ($rawLatestTag)")
                return@withContext Result.success(null)
            }

            if (!ignoreDismissed && isVersionDismissed(rawLatestTag)) {
                Log.d(TAG, "Update $rawLatestTag was previously dismissed by user")
                return@withContext Result.success(null)
            }

            // Find direct APK asset if available
            val apkAsset = release.assets.firstOrNull { it.name.endsWith(".apk", ignoreCase = true) }
            val downloadUrl = apkAsset?.browserDownloadUrl ?: release.htmlUrl

            val updateInfo = AppUpdateInfo(
                isUpdateAvailable = true,
                latestVersion = rawLatestTag,
                currentVersion = currentVersion,
                releaseTitle = release.name ?: "ValoMobile $rawLatestTag",
                releaseNotes = release.body ?: "A new update is available on GitHub.",
                downloadUrl = downloadUrl,
                releasePageUrl = release.htmlUrl
            )

            Log.d(TAG, "New app update available: $rawLatestTag (current: $currentVersion)")
            return@withContext Result.success(updateInfo)
        } catch (e: Exception) {
            Log.e(TAG, "Error checking for GitHub app updates", e)
            return@withContext Result.failure(e)
        }
    }

    fun dismissUpdate(version: String) {
        prefs.edit().putString(KEY_DISMISSED_VERSION, version.trim()).apply()
    }

    fun isVersionDismissed(version: String): Boolean {
        val dismissed = prefs.getString(KEY_DISMISSED_VERSION, null) ?: return false
        return dismissed.equals(version.trim(), ignoreCase = true)
    }

    /**
     * Compares two semantic version strings (e.g. "1.4.2" vs "1.4.1")
     * Returns true if latest > current.
     */
    fun isNewerVersion(latest: String, current: String): Boolean {
        if (latest.isBlank() || current.isBlank()) return false
        if (latest.equals(current, ignoreCase = true)) return false

        val latestParts = latest.split(".").mapNotNull { it.takeWhile { char -> char.isDigit() }.toIntOrNull() }
        val currentParts = current.split(".").mapNotNull { it.takeWhile { char -> char.isDigit() }.toIntOrNull() }

        val length = maxOf(latestParts.size, currentParts.size)
        for (i in 0 until length) {
            val l = latestParts.getOrElse(i) { 0 }
            val c = currentParts.getOrElse(i) { 0 }
            if (l > c) return true
            if (l < c) return false
        }

        return false
    }
}
