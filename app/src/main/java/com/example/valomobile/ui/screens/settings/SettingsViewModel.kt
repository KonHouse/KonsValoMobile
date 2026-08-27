package com.example.valomobile.ui.screens.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.valomobile.BuildConfig
import com.example.valomobile.data.repository.AppUpdateRepository
import com.example.valomobile.data.repository.RiotAuthRepository
import com.example.valomobile.domain.model.AppUpdateInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val riotAuthRepository: RiotAuthRepository,
    private val appUpdateRepository: AppUpdateRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val prefs = context.getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)
    
    private val _wishlistNotifications = MutableStateFlow(prefs.getBoolean("wishlist_notifications", true))
    val wishlistNotifications: StateFlow<Boolean> = _wishlistNotifications

    private val _isCheckingUpdate = MutableStateFlow(false)
    val isCheckingUpdate: StateFlow<Boolean> = _isCheckingUpdate.asStateFlow()

    private val _manualUpdateResult = MutableStateFlow<AppUpdateInfo?>(null)
    val manualUpdateResult: StateFlow<AppUpdateInfo?> = _manualUpdateResult.asStateFlow()

    private val _updateMessage = MutableStateFlow<String?>(null)
    val updateMessage: StateFlow<String?> = _updateMessage.asStateFlow()

    fun getRiotId(): String {
        val name = riotAuthRepository.getGameName() ?: "Valorant"
        val tag = riotAuthRepository.getTagLine() ?: "EU"
        return "$name#$tag"
    }

    fun getRegion(): String {
        return riotAuthRepository.getRegion().uppercase()
    }

    fun toggleWishlistNotifications() {
        val newValue = !_wishlistNotifications.value
        prefs.edit().putBoolean("wishlist_notifications", newValue).apply()
        _wishlistNotifications.value = newValue
    }

    fun checkManualUpdate() {
        viewModelScope.launch {
            _isCheckingUpdate.value = true
            _updateMessage.value = null
            try {
                val res = appUpdateRepository.checkForUpdate(ignoreDismissed = true)
                res.fold(
                    onSuccess = { update ->
                        if (update != null) {
                            _manualUpdateResult.value = update
                        } else {
                            _updateMessage.value = "You're on the latest version (v${BuildConfig.VERSION_NAME})! 🚀"
                        }
                    },
                    onFailure = { err ->
                        _updateMessage.value = "Could not check updates: ${err.message}"
                    }
                )
            } catch (e: Exception) {
                _updateMessage.value = "Error checking updates."
            } finally {
                _isCheckingUpdate.value = false
            }
        }
    }

    fun dismissManualUpdateDialog() {
        _manualUpdateResult.value = null
    }

    fun clearUpdateMessage() {
        _updateMessage.value = null
    }

    fun logout() {
        riotAuthRepository.logout()
    }
}
