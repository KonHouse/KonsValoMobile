package com.example.valomobile.ui.screens.store

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.valomobile.data.remote.model.ValorantChroma
import com.example.valomobile.data.remote.model.ValorantLevel
import com.example.valomobile.data.remote.model.ValorantSkin
import com.example.valomobile.data.repository.SkinCatalogRepository
import com.example.valomobile.domain.model.SkinItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface SkinDetailState {
    data object Loading : SkinDetailState
    data class Success(
        val skin: ValorantSkin,
        val selectedChroma: ValorantChroma?,
        val selectedLevel: ValorantLevel?,
        val activeVideoUrl: String?
    ) : SkinDetailState
    data class Error(val message: String) : SkinDetailState
}

@HiltViewModel
class SkinDetailViewModel @Inject constructor(
    private val catalogRepository: SkinCatalogRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<SkinDetailState>(SkinDetailState.Loading)
    val uiState: StateFlow<SkinDetailState> = _uiState.asStateFlow()

    fun loadSkinDetails(skinItem: SkinItem) {
        viewModelScope.launch {
            _uiState.value = SkinDetailState.Loading
            try {
                val skinUuid = catalogRepository.getSkinUuidForLevel(skinItem.uuid)
                val fullSkin = catalogRepository.getSkinDetails(skinUuid) 
                    ?: catalogRepository.getSkinDetails(skinItem.skinUuid)
                
                if (fullSkin != null) {
                    val defaultChroma = fullSkin.chromas.firstOrNull()
                    val defaultLevelWithVideo = fullSkin.levels.lastOrNull { !it.streamedVideo.isNullOrBlank() } 
                        ?: fullSkin.levels.firstOrNull()
                    val defaultVideo = defaultLevelWithVideo?.streamedVideo 
                        ?: defaultChroma?.streamedVideo

                    _uiState.value = SkinDetailState.Success(
                        skin = fullSkin,
                        selectedChroma = defaultChroma,
                        selectedLevel = defaultLevelWithVideo,
                        activeVideoUrl = defaultVideo
                    )
                } else {
                    _uiState.value = SkinDetailState.Error("Skin details unavailable")
                }
            } catch (e: Exception) {
                _uiState.value = SkinDetailState.Error(e.message ?: "Failed to load skin details")
            }
        }
    }

    fun selectLevel(level: ValorantLevel) {
        val current = _uiState.value as? SkinDetailState.Success ?: return
        val videoUrl = level.streamedVideo
        _uiState.value = current.copy(
            selectedLevel = level,
            activeVideoUrl = videoUrl
        )
    }

    fun selectChroma(chroma: ValorantChroma) {
        val current = _uiState.value as? SkinDetailState.Success ?: return
        val videoUrl = chroma.streamedVideo ?: current.selectedLevel?.streamedVideo
        _uiState.value = current.copy(
            selectedChroma = chroma,
            activeVideoUrl = videoUrl
        )
    }
}
