package com.example.valomobile.ui.screens.store

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.valomobile.data.local.WishlistDao
import com.example.valomobile.data.local.WishlistEntity
import com.example.valomobile.data.repository.RiotAuthRepository
import com.example.valomobile.data.repository.RiotStoreRepository
import com.example.valomobile.data.repository.SkinCatalogRepository
import com.example.valomobile.domain.model.Bundle
import com.example.valomobile.domain.model.SkinItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class StoreViewModel @Inject constructor(
    private val repository: RiotStoreRepository,
    private val authRepository: RiotAuthRepository,
    private val catalogRepository: SkinCatalogRepository,
    private val wishlistDao: WishlistDao
) : ViewModel() {

    private val _storeRotation = MutableStateFlow<List<SkinItem>>(emptyList())
    val storeRotation: StateFlow<List<SkinItem>> = _storeRotation.asStateFlow()

    private val _featuredBundles = MutableStateFlow<List<Bundle>>(emptyList())
    val featuredBundles: StateFlow<List<Bundle>> = _featuredBundles.asStateFlow()

    private val _nightMarket = MutableStateFlow<List<SkinItem>>(emptyList())
    val nightMarket: StateFlow<List<SkinItem>> = _nightMarket.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    val wishlist: StateFlow<Set<String>> = wishlistDao.getAllWishlistItems()
        .map { items -> items.map { it.uuid }.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    init {
        // Automatically reload store whenever user logs in or session updates
        viewModelScope.launch {
            authRepository.sessionState.collect { isLogged ->
                if (isLogged) {
                    loadData()
                } else {
                    _storeRotation.value = emptyList()
                    _featuredBundles.value = emptyList()
                    _nightMarket.value = emptyList()
                }
            }
        }
    }

    fun loadData() {
        if (!authRepository.isLoggedIn) return

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val rotation = withContext(Dispatchers.IO) { repository.getStoreRotation() }
                val bundles = withContext(Dispatchers.IO) { repository.getFeaturedBundles() }
                val nightMarket = withContext(Dispatchers.IO) { repository.getNightMarket() }

                val levelToSkinMap: Map<String, String> = catalogRepository.getLevelToSkinMap()
                _storeRotation.value = rotation.map { item -> 
                    item.copy(skinUuid = levelToSkinMap[item.uuid] ?: item.uuid) 
                }
                _featuredBundles.value = bundles.map { bundle ->
                    bundle.copy(items = bundle.items.map { item -> 
                        item.copy(skinUuid = levelToSkinMap[item.uuid] ?: item.uuid) 
                    })
                }
                _nightMarket.value = nightMarket.map { item -> 
                    item.copy(skinUuid = levelToSkinMap[item.uuid] ?: item.uuid) 
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load Valorant store"
            } finally {
                _isLoading.value = false
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
}
