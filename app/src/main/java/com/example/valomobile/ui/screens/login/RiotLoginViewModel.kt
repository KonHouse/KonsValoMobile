package com.example.valomobile.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.valomobile.data.repository.RiotAuthRepository
import com.example.valomobile.data.repository.RiotAuthResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface LoginState {
    data object Initial : LoginState
    data class Loading(val message: String = "Logowanie do Riot Games...") : LoginState
    data class TwoFaRequired(val message: String, val email: String? = null) : LoginState
    data class Success(val gameName: String, val tagLine: String) : LoginState
    data class Error(val message: String) : LoginState
}

@HiltViewModel
class RiotLoginViewModel @Inject constructor(
    private val authRepository: RiotAuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginState>(LoginState.Initial)
    val uiState: StateFlow<LoginState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.sessionState.collect { isLogged ->
                if (isLogged) {
                    val name = authRepository.getGameName() ?: "Valorant"
                    val tag = authRepository.getTagLine() ?: "EU"
                    _uiState.value = LoginState.Success(name, tag)
                }
            }
        }
    }

    fun isLoggedIn(): Boolean {
        return authRepository.isLoggedIn
    }

    fun resetState() {
        _uiState.value = LoginState.Initial
    }

    fun loginWithCredentials(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            _uiState.value = LoginState.Error("Wpisz nazwę konta Riot oraz hasło.")
            return
        }

        _uiState.value = LoginState.Loading("Łączenie z Riot Games...")

        viewModelScope.launch {
            when (val result = authRepository.login(username, password)) {
                is RiotAuthResult.Success -> {
                    _uiState.value = LoginState.Success(result.gameName, result.tagLine)
                }
                is RiotAuthResult.TwoFaRequired -> {
                    _uiState.value = LoginState.TwoFaRequired(result.message, result.email)
                }
                is RiotAuthResult.Error -> {
                    _uiState.value = LoginState.Error(result.message)
                }
            }
        }
    }

    fun submit2FaCode(code: String) {
        if (code.isBlank()) {
            _uiState.value = LoginState.Error("Wprowadź 6-cyfrowy kod 2FA.")
            return
        }

        _uiState.value = LoginState.Loading("Weryfikowanie kodu 2FA...")

        viewModelScope.launch {
            when (val result = authRepository.submit2Fa(code)) {
                is RiotAuthResult.Success -> {
                    _uiState.value = LoginState.Success(result.gameName, result.tagLine)
                }
                is RiotAuthResult.TwoFaRequired -> {
                    _uiState.value = LoginState.TwoFaRequired(result.message, result.email)
                }
                is RiotAuthResult.Error -> {
                    _uiState.value = LoginState.Error(result.message)
                }
            }
        }
    }

    fun loginWithRedirectUrl(url: String) {
        if (url.isBlank()) {
            _uiState.value = LoginState.Error("Wklej przekierowany adres URL po zalogowaniu.")
            return
        }

        _uiState.value = LoginState.Loading("Pobieranie tokenów sesji...")

        viewModelScope.launch {
            when (val result = authRepository.loginWithRedirectUrl(url)) {
                is RiotAuthResult.Success -> {
                    _uiState.value = LoginState.Success(result.gameName, result.tagLine)
                }
                is RiotAuthResult.TwoFaRequired -> {
                    _uiState.value = LoginState.TwoFaRequired(result.message, result.email)
                }
                is RiotAuthResult.Error -> {
                    _uiState.value = LoginState.Error(result.message)
                }
            }
        }
    }

    fun loginWithTokens(accessToken: String, idToken: String) {
        _uiState.value = LoginState.Loading("Autoryzacja konta Riot...")

        viewModelScope.launch {
            when (val result = authRepository.loginWithTokens(accessToken, idToken)) {
                is RiotAuthResult.Success -> {
                    _uiState.value = LoginState.Success(result.gameName, result.tagLine)
                }
                is RiotAuthResult.TwoFaRequired -> {
                    _uiState.value = LoginState.TwoFaRequired(result.message, result.email)
                }
                is RiotAuthResult.Error -> {
                    _uiState.value = LoginState.Error(result.message)
                }
            }
        }
    }
}
