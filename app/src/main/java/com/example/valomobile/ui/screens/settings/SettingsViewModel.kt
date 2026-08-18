package com.example.valomobile.ui.screens.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.valomobile.data.repository.RiotAuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val riotAuthRepository: RiotAuthRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val prefs = context.getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)
    
    private val _wishlistNotifications = MutableStateFlow(prefs.getBoolean("wishlist_notifications", true))
    val wishlistNotifications: StateFlow<Boolean> = _wishlistNotifications

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

    fun logout() {
        riotAuthRepository.logout()
    }
}
