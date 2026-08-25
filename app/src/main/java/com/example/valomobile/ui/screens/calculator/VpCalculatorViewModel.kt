package com.example.valomobile.ui.screens.calculator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.valomobile.data.local.WishlistDao
import com.example.valomobile.data.repository.RiotStoreRepository
import com.example.valomobile.data.repository.SkinCatalogRepository
import com.example.valomobile.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class VpCalculatorViewModel @Inject constructor(
    private val catalogRepository: SkinCatalogRepository,
    private val storeRepository: RiotStoreRepository,
    private val wishlistDao: WishlistDao
) : ViewModel() {

    private val _selectedSkins = MutableStateFlow<List<SkinItem>>(emptyList())
    val selectedSkins: StateFlow<List<SkinItem>> = _selectedSkins.asStateFlow()

    private val _currentVp = MutableStateFlow(0)
    val currentVp: StateFlow<Int> = _currentVp.asStateFlow()

    private val _currency = MutableStateFlow(CurrencyType.PLN)
    val currency: StateFlow<CurrencyType> = _currency.asStateFlow()

    private val _isNightMarketAvailable = MutableStateFlow(false)
    val isNightMarketAvailable: StateFlow<Boolean> = _isNightMarketAvailable.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _catalogSkins = MutableStateFlow<List<SkinItem>>(emptyList())
    private var cachedLevelMap: Map<String, String> = emptyMap()
    
    val searchResults: StateFlow<List<SkinItem>> = combine(_searchQuery, _catalogSkins) { query, skins ->
        if (query.isBlank()) {
            emptyList()
        } else {
            skins.filter { it.displayName.contains(query, ignoreCase = true) }.take(20)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val calculationResult: StateFlow<VpCalculationResult> = combine(
        _selectedSkins,
        _currentVp,
        _currency
    ) { skins, vp, curr ->
        val totalCost = skins.sumOf { if (it.finalPrice > 0) it.finalPrice else it.price }
        VpPacksCatalog.calculateOptimalPacks(
            totalCostVp = totalCost,
            currentVp = vp,
            currency = curr
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        VpCalculationResult()
    )

    init {
        // Load catalog, initial wallet balance, level map, and check night market
        viewModelScope.launch {
            try {
                val skins = withContext(Dispatchers.IO) { catalogRepository.getAllSkins() }
                _catalogSkins.value = skins
                cachedLevelMap = withContext(Dispatchers.IO) { catalogRepository.getLevelToSkinMap() }
                val wallet = withContext(Dispatchers.IO) { storeRepository.getWallet() }
                if (wallet.vp > 0) {
                    _currentVp.value = wallet.vp
                }
                val nm = withContext(Dispatchers.IO) { storeRepository.getNightMarket() }
                _isNightMarketAvailable.value = nm.isNotEmpty()
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    private fun getCanonicalSkinId(item: SkinItem): String {
        val mappedSkinUuid = cachedLevelMap[item.uuid.lowercase()]
            ?: cachedLevelMap[item.skinUuid.lowercase()]
            ?: item.skinUuid.ifBlank { item.uuid }.lowercase()
        return if (mappedSkinUuid.isNotBlank()) mappedSkinUuid else item.displayName.lowercase()
    }

    private fun mergeSkins(current: List<SkinItem>, newItems: List<SkinItem>): List<SkinItem> {
        val existingKeys = current.map { getCanonicalSkinId(it) }.toMutableSet()
        val result = current.toMutableList()
        for (item in newItems) {
            val key = getCanonicalSkinId(item)
            if (!existingKeys.contains(key)) {
                existingKeys.add(key)
                result.add(item)
            }
        }
        return result
    }

    fun addSkin(skin: SkinItem) {
        val current = _selectedSkins.value
        val skinKey = getCanonicalSkinId(skin)
        if (current.none { getCanonicalSkinId(it) == skinKey }) {
            _selectedSkins.value = current + skin
        }
    }

    fun removeSkin(skin: SkinItem) {
        val key = getCanonicalSkinId(skin)
        _selectedSkins.value = _selectedSkins.value.filter { getCanonicalSkinId(it) != key }
    }

    fun clearAll() {
        _selectedSkins.value = emptyList()
    }

    fun setCurrentVp(vp: Int) {
        _currentVp.value = vp.coerceAtLeast(0)
    }

    fun setCurrency(currencyType: CurrencyType) {
        _currency.value = currencyType
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun importWishlist() {
        viewModelScope.launch {
            try {
                val wishlistEntities = withContext(Dispatchers.IO) { wishlistDao.getAllWishlistItems().first() }
                val allSkins = _catalogSkins.value.associateBy { it.skinUuid.lowercase() }
                val levelToSkinMap = if (cachedLevelMap.isNotEmpty()) cachedLevelMap else withContext(Dispatchers.IO) { catalogRepository.getLevelToSkinMap() }

                val importedSkins = wishlistEntities.mapNotNull { entity ->
                    val skinUuid = levelToSkinMap[entity.uuid.lowercase()] ?: entity.uuid.lowercase()
                    allSkins[skinUuid] ?: SkinItem(
                        uuid = entity.uuid,
                        displayName = entity.name,
                        displayIcon = entity.displayIcon ?: "",
                        weaponType = "Weapon",
                        price = entity.price,
                        discount = 0,
                        tier = "Select",
                        skinUuid = skinUuid
                    )
                }
                
                _selectedSkins.value = mergeSkins(_selectedSkins.value, importedSkins)
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    fun importStoreRotation() {
        viewModelScope.launch {
            try {
                val storeSkins = withContext(Dispatchers.IO) { storeRepository.getStoreRotation() }
                _selectedSkins.value = mergeSkins(_selectedSkins.value, storeSkins)
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    fun importFeaturedBundles() {
        viewModelScope.launch {
            try {
                val bundles = withContext(Dispatchers.IO) { storeRepository.getFeaturedBundles() }
                val bundleSkinItems = bundles.map { bundle ->
                    SkinItem(
                        uuid = bundle.uuid.ifBlank { "bundle_${bundle.displayName}" },
                        displayName = bundle.displayName,
                        displayIcon = bundle.displayIcon,
                        weaponType = "Bundle",
                        price = bundle.price,
                        discount = 0,
                        tier = "Exclusive",
                        skinUuid = bundle.uuid
                    )
                }
                _selectedSkins.value = mergeSkins(_selectedSkins.value, bundleSkinItems)
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    fun importNightMarket() {
        viewModelScope.launch {
            try {
                val nightMarketSkins = withContext(Dispatchers.IO) { storeRepository.getNightMarket() }
                if (nightMarketSkins.isNotEmpty()) {
                    _selectedSkins.value = mergeSkins(_selectedSkins.value, nightMarketSkins)
                }
            } catch (e: Exception) {
                // ignore
            }
        }
    }
}
