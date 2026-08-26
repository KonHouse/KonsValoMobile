package com.example.valomobile.ui.screens.friends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.valomobile.data.repository.RiotFriendsRepository
import com.example.valomobile.domain.model.FriendsGrouped
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FriendsViewModel @Inject constructor(
    private val friendsRepository: RiotFriendsRepository
) : ViewModel() {

    private val _friendsGrouped = MutableStateFlow(FriendsGrouped())
    val friendsGrouped: StateFlow<FriendsGrouped> = _friendsGrouped.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private var autoRefreshJob: Job? = null

    val filteredFriends: StateFlow<FriendsGrouped> = combine(_friendsGrouped, _searchQuery) { grouped, query ->
        if (query.isBlank()) {
            grouped
        } else {
            val q = query.trim().lowercase()
            FriendsGrouped(
                inGame = grouped.inGame.filter { it.riotId.lowercase().contains(q) || it.rankName.lowercase().contains(q) },
                online = grouped.online.filter { it.riotId.lowercase().contains(q) || it.rankName.lowercase().contains(q) },
                offline = grouped.offline.filter { it.riotId.lowercase().contains(q) }
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FriendsGrouped())

    init {
        loadFriends(silent = false)
        startPeriodicAutoRefresh()
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun loadFriends(silent: Boolean = false) {
        viewModelScope.launch {
            if (!silent) {
                _isLoading.value = true
                _error.value = null
            }
            try {
                val result = friendsRepository.getFriendsGrouped()
                _friendsGrouped.value = result
                if (silent) _error.value = null
            } catch (e: Exception) {
                if (!silent) {
                    _error.value = e.message ?: "Failed to load friends list"
                }
            } finally {
                if (!silent) {
                    _isLoading.value = false
                }
            }
        }
    }

    private fun startPeriodicAutoRefresh() {
        autoRefreshJob?.cancel()
        autoRefreshJob = viewModelScope.launch {
            while (true) {
                delay(25_000) // 25 seconds live presence polling
                loadFriends(silent = true)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        autoRefreshJob?.cancel()
    }
}
