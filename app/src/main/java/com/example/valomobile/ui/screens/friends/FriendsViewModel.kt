package com.example.valomobile.ui.screens.friends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.valomobile.data.repository.CloudFriendsRepository
import com.example.valomobile.domain.model.CloudUserProfile
import com.example.valomobile.domain.model.FriendInvite
import com.example.valomobile.domain.model.InAppFriendItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FriendsViewModel @Inject constructor(
    private val cloudFriendsRepository: CloudFriendsRepository
) : ViewModel() {

    private val _myFriendCode = MutableStateFlow("VALO-....")
    val myFriendCode: StateFlow<String> = _myFriendCode.asStateFlow()

    val incomingInvites: StateFlow<List<FriendInvite>> = cloudFriendsRepository.observeIncomingInvites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val friendsList: StateFlow<List<InAppFriendItem>> = cloudFriendsRepository.observeFriendsWithStores()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _inviteInput = MutableStateFlow("")
    val inviteInput: StateFlow<String> = _inviteInput.asStateFlow()

    private val _isSendingInvite = MutableStateFlow(false)
    val isSendingInvite: StateFlow<Boolean> = _isSendingInvite.asStateFlow()

    private val _selectedFriendForStoreModal = MutableStateFlow<InAppFriendItem?>(null)
    val selectedFriendForStoreModal: StateFlow<InAppFriendItem?> = _selectedFriendForStoreModal.asStateFlow()

    private val _actionMessage = MutableStateFlow<String?>(null)
    val actionMessage: StateFlow<String?> = _actionMessage.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    val filteredFriends: StateFlow<List<InAppFriendItem>> = combine(friendsList, searchQuery) { friends, query ->
        if (query.isBlank()) {
            friends
        } else {
            val q = query.trim().lowercase()
            friends.filter {
                it.riotId.lowercase().contains(q) ||
                it.friendCode.lowercase().contains(q) ||
                it.storeOffers.any { skin -> skin.displayName.lowercase().contains(q) }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadMyFriendCode()
    }

    fun loadMyFriendCode() {
        viewModelScope.launch {
            try {
                val code = cloudFriendsRepository.getMyFriendCode()
                _myFriendCode.value = code
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun onInviteInputChange(newInput: String) {
        _inviteInput.value = newInput
    }

    fun sendInvite() {
        val input = _inviteInput.value.trim()
        if (input.isBlank()) {
            _errorMessage.value = "Enter a Friend Code (e.g. VALO-1234) or Riot ID."
            return
        }

        viewModelScope.launch {
            _isSendingInvite.value = true
            _errorMessage.value = null
            _actionMessage.value = null
            try {
                val result = cloudFriendsRepository.sendFriendInvite(input)
                result.fold(
                    onSuccess = { msg ->
                        _actionMessage.value = msg
                        _inviteInput.value = ""
                    },
                    onFailure = { err ->
                        _errorMessage.value = err.message ?: "Failed to send invite"
                    }
                )
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to send invite"
            } finally {
                _isSendingInvite.value = false
            }
        }
    }

    fun acceptInvite(invite: FriendInvite) {
        viewModelScope.launch {
            _errorMessage.value = null
            val result = cloudFriendsRepository.acceptInvite(invite)
            result.fold(
                onSuccess = {
                    _actionMessage.value = "You are now friends with ${invite.fromRiotId}!"
                },
                onFailure = { err ->
                    _errorMessage.value = "Failed to accept: ${err.message}"
                }
            )
        }
    }

    fun declineInvite(invite: FriendInvite) {
        viewModelScope.launch {
            cloudFriendsRepository.declineInvite(invite)
            _actionMessage.value = "Invite declined."
        }
    }

    fun removeFriend(friendPuuid: String) {
        viewModelScope.launch {
            cloudFriendsRepository.removeFriend(friendPuuid)
            _actionMessage.value = "Friend removed."
            if (_selectedFriendForStoreModal.value?.puuid == friendPuuid) {
                _selectedFriendForStoreModal.value = null
            }
        }
    }

    fun openFriendStoreModal(friend: InAppFriendItem) {
        _selectedFriendForStoreModal.value = friend
    }

    fun dismissFriendStoreModal() {
        _selectedFriendForStoreModal.value = null
    }

    fun clearMessages() {
        _actionMessage.value = null
        _errorMessage.value = null
    }
}
