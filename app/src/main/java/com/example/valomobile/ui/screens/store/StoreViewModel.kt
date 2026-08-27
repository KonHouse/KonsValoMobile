package com.example.valomobile.ui.screens.store

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.valomobile.data.local.WishlistDao
import com.example.valomobile.data.local.WishlistEntity
import com.example.valomobile.data.remote.model.UserWallet
import com.example.valomobile.data.repository.AppUpdateRepository
import com.example.valomobile.data.repository.RiotAuthRepository
import com.example.valomobile.data.repository.RiotStoreRepository
import com.example.valomobile.data.repository.SkinCatalogRepository
import com.example.valomobile.domain.model.AppUpdateInfo
import com.example.valomobile.domain.model.Bundle
import com.example.valomobile.domain.model.SkinItem
import com.example.valomobile.data.repository.CloudFriendsRepository
import com.example.valomobile.data.repository.DailyStreakRepository
import com.example.valomobile.domain.model.DailyStreakInfo
import com.example.valomobile.domain.model.StreakCheckInResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class StoreViewModel @Inject constructor(
    private val repository: RiotStoreRepository,
    private val authRepository: RiotAuthRepository,
    private val catalogRepository: SkinCatalogRepository,
    private val wishlistDao: WishlistDao,
    private val dailyStreakRepository: DailyStreakRepository,
    private val cloudFriendsRepository: CloudFriendsRepository,
    private val appUpdateRepository: AppUpdateRepository
) : ViewModel() {

    companion object {
        private const val AUTO_REFRESH_INTERVAL_MS = 45_000L // 45 seconds periodic check
    }

    private val _storeRotation = MutableStateFlow<List<SkinItem>>(emptyList())
    val storeRotation: StateFlow<List<SkinItem>> = _storeRotation.asStateFlow()

    private val _featuredBundles = MutableStateFlow<List<Bundle>>(emptyList())
    val featuredBundles: StateFlow<List<Bundle>> = _featuredBundles.asStateFlow()

    private val _nightMarket = MutableStateFlow<List<SkinItem>>(emptyList())
    val nightMarket: StateFlow<List<SkinItem>> = _nightMarket.asStateFlow()

    private val _wallet = MutableStateFlow(UserWallet())
    val wallet: StateFlow<UserWallet> = _wallet.asStateFlow()

    val streakInfo: StateFlow<DailyStreakInfo> = dailyStreakRepository.streakInfo

    private val _celebrationEvent = MutableStateFlow<StreakCheckInResult?>(null)
    val celebrationEvent: StateFlow<StreakCheckInResult?> = _celebrationEvent.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private var autoRefreshJob: Job? = null

    val isLoggedIn: Boolean
        get() = authRepository.isLoggedIn

    val wishlist: StateFlow<Set<String>> = wishlistDao.getAllWishlistItems()
        .map { items -> items.map { it.uuid }.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    private val _appUpdateInfo = MutableStateFlow<AppUpdateInfo?>(null)
    val appUpdateInfo: StateFlow<AppUpdateInfo?> = _appUpdateInfo.asStateFlow()

    init {
        // Automatically check daily streak on startup
        checkDailyStreak()

        // Check for new releases on GitHub
        checkForAppUpdates()

        // Automatically reload store whenever user logs in or session updates
        viewModelScope.launch {
            authRepository.sessionState.collect { isLogged ->
                if (isLogged) {
                    val puuid = authRepository.getPuuid() ?: ""
                    dailyStreakRepository.refreshStreakForAccount(puuid)
                    checkDailyStreak()
                    loadData(silent = false)
                    startPeriodicAutoRefresh()
                } else {
                    stopPeriodicAutoRefresh()
                    _storeRotation.value = emptyList()
                    _featuredBundles.value = emptyList()
                    _nightMarket.value = emptyList()
                    _wallet.value = UserWallet()
                }
            }
        }
    }

    fun checkDailyStreak() {
        val puuid = authRepository.getPuuid() ?: ""
        val result = dailyStreakRepository.recordDailyVisit(puuid = puuid)
        if (result.isNewDay && result.isStreakIncreased) {
            _celebrationEvent.value = result
        }
    }

    fun dismissCelebration() {
        _celebrationEvent.value = null
    }

    fun checkForAppUpdates() {
        viewModelScope.launch {
            try {
                val result = appUpdateRepository.checkForUpdate()
                result.getOrNull()?.let { update ->
                    _appUpdateInfo.value = update
                }
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    fun dismissAppUpdate() {
        _appUpdateInfo.value?.let { update ->
            appUpdateRepository.dismissUpdate(update.latestVersion)
        }
        _appUpdateInfo.value = null
    }

    private fun startPeriodicAutoRefresh() {
        autoRefreshJob?.cancel()
        autoRefreshJob = viewModelScope.launch {
            while (isActive) {
                delay(AUTO_REFRESH_INTERVAL_MS)
                if (authRepository.isLoggedIn) {
                    loadData(silent = true)
                }
            }
        }
    }

    private fun stopPeriodicAutoRefresh() {
        autoRefreshJob?.cancel()
        autoRefreshJob = null
    }

    fun loadData(silent: Boolean = false) {
        viewModelScope.launch {
            if (!silent) {
                _isLoading.value = true
                _error.value = null
            }
            try {
                if (!authRepository.isLoggedIn) {
                    val refreshed = withContext(Dispatchers.IO) { authRepository.refreshSessionSilently() }
                    if (!refreshed && !authRepository.isLoggedIn) {
                        if (!silent) {
                            throw IOException("Riot session expired. Please tap 'Check Your Shop' to reconnect.")
                        } else {
                            return@launch
                        }
                    }
                }

                val rotation = withContext(Dispatchers.IO) { repository.getStoreRotation() }
                val bundles = withContext(Dispatchers.IO) { repository.getFeaturedBundles() }
                val nightMarket = withContext(Dispatchers.IO) { repository.getNightMarket() }
                val userWallet = withContext(Dispatchers.IO) { repository.getWallet() }

                val levelToSkinMap: Map<String, String> = catalogRepository.getLevelToSkinMap()
                val newRotation = rotation.map { item -> 
                    item.copy(skinUuid = levelToSkinMap[item.uuid] ?: item.uuid) 
                }
                val newBundles = bundles.map { bundle ->
                    bundle.copy(items = bundle.items.map { item -> 
                        item.copy(skinUuid = levelToSkinMap[item.uuid] ?: item.uuid) 
                    })
                }
                val newNightMarket = nightMarket.map { item -> 
                    item.copy(skinUuid = levelToSkinMap[item.uuid] ?: item.uuid) 
                }

                // Update only if data actually changed (avoids unnecessary recompositions)
                if (_storeRotation.value != newRotation) {
                    _storeRotation.value = newRotation
                }
                if (_featuredBundles.value != newBundles) {
                    _featuredBundles.value = newBundles
                }
                if (_nightMarket.value != newNightMarket) {
                    _nightMarket.value = newNightMarket
                }
                if (_wallet.value != userWallet) {
                    _wallet.value = userWallet
                }

                // Automatically archive today's store in local history
                val puuid = authRepository.getPuuid()
                if (!puuid.isNullOrBlank() && newRotation.isNotEmpty()) {
                    dailyStreakRepository.saveTodayStore(newRotation, puuid)
                    try {
                        cloudFriendsRepository.syncUserProfileAndStore(
                            skinOffers = newRotation,
                            streakCount = streakInfo.value.currentStreak
                        )
                    } catch (e: Exception) {
                        // ignore cloud sync error to keep store offline functional
                    }
                }
                if (silent) {
                    _error.value = null
                }
            } catch (e: Exception) {
                if (!silent) {
                    _error.value = e.message ?: "Failed to load Valorant store"
                }
            } finally {
                if (!silent) {
                    _isLoading.value = false
                }
            }
        }
    }

    fun toggleWishlist(item: SkinItem) {
        viewModelScope.launch {
            val isWishlisted = wishlist.value.contains(item.skinUuid)
            if (isWishlisted) {
                wishlistDao.deleteByUuid(item.skinUuid)
            } else {
                wishlistDao.insert(
                    WishlistEntity(
                        uuid = item.skinUuid,
                        name = item.displayName,
                        displayIcon = item.displayIcon,
                        price = item.finalPrice
                    )
                )
            }
        }
    }

    fun isWishlisted(skinUuid: String): Boolean {
        return wishlist.value.contains(skinUuid)
    }

    fun getRecordedDates(): Flow<Set<String>> {
        val puuid = authRepository.getPuuid() ?: return flowOf(emptySet())
        return dailyStreakRepository.getRecordedDates(puuid)
    }

    suspend fun getStoreHistoryForDate(date: java.time.LocalDate): List<SkinItem>? {
        val puuid = authRepository.getPuuid() ?: return null
        return dailyStreakRepository.getStoreHistoryForDate(date, puuid)
    }

    override fun onCleared() {
        super.onCleared()
        stopPeriodicAutoRefresh()
    }
}
