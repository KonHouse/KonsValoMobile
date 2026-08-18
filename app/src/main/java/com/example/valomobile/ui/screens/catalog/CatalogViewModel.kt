package com.example.valomobile.ui.screens.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.valomobile.data.local.WishlistDao
import com.example.valomobile.data.local.WishlistEntity
import com.example.valomobile.data.repository.SkinCatalogRepository
import com.example.valomobile.domain.model.SkinItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CatalogViewModel @Inject constructor(
    private val catalogRepository: SkinCatalogRepository,
    private val wishlistDao: WishlistDao
) : ViewModel() {

    private val _allSkins = MutableStateFlow<List<SkinItem>>(emptyList())
    private val _searchQuery = MutableStateFlow("")
    private val _sortType = MutableStateFlow(SortType.NAME_ASC)
    private val _isLoading = MutableStateFlow(false)

    val searchQuery = _searchQuery.asStateFlow()
    val sortType = _sortType.asStateFlow()
    val isLoading = _isLoading.asStateFlow()

    val wishlist = wishlistDao.getAllWishlistItems()
        .map { list -> list.map { it.uuid }.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val filteredSkins = combine(_allSkins, _searchQuery, _sortType) { skins, query, sort ->
        var result = skins.filter { it.displayName.contains(query, ignoreCase = true) }
        result = when (sort) {
            SortType.NAME_ASC -> result.sortedBy { it.displayName }
            SortType.NAME_DESC -> result.sortedByDescending { it.displayName }
            SortType.TIER -> result.sortedBy { it.tier }
        }
        result
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val wishlistItems = combine(wishlistDao.getAllWishlistItems(), _allSkins) { localItems, remoteSkins ->
        localItems.map { entity ->
            val remoteSkin = remoteSkins.find { it.skinUuid == entity.uuid }
            SkinItem(
                uuid = entity.uuid,
                displayName = remoteSkin?.displayName ?: entity.name,
                displayIcon = remoteSkin?.displayIcon ?: entity.displayIcon,
                weaponType = "Weapon",
                price = entity.price,
                discount = 0,
                tier = remoteSkin?.tier ?: entity.tier,
                skinUuid = entity.uuid
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadSkins()
    }

    private fun loadSkins() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _allSkins.value = catalogRepository.getAllSkins()
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onSortTypeChange(sortType: SortType) {
        _sortType.value = sortType
    }

    fun toggleWishlist(skin: SkinItem) {
        viewModelScope.launch {
            if (wishlist.value.contains(skin.skinUuid)) {
                wishlistDao.deleteByUuid(skin.skinUuid)
            } else {
                wishlistDao.insert(
                    WishlistEntity(
                        uuid = skin.skinUuid,
                        name = skin.displayName,
                        displayIcon = skin.displayIcon,
                        price = skin.price,
                        tier = skin.tier
                    )
                )
            }
        }
    }

    fun removeFromWishlist(uuid: String) {
        viewModelScope.launch {
            wishlistDao.deleteByUuid(uuid)
        }
    }

    enum class SortType {
        NAME_ASC, NAME_DESC, TIER
    }
}
