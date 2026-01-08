package com.ngt.pixplay.ui.screens.discover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ngt.pixplay.data.model.Album
import com.ngt.pixplay.data.model.AudioItem
import com.ngt.pixplay.data.repository.MusicRepository
import com.ngt.pixplay.service.PlaybackStateManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DiscoverViewModel @Inject constructor(
    private val musicRepository: MusicRepository,
    private val playbackStateManager: PlaybackStateManager
) : ViewModel() {
    
    data class DiscoverUiState(
        val isLoading: Boolean = true,
        val forYou: List<AudioItem> = emptyList(),
        val reconnect: List<AudioItem> = emptyList(),
        val mostPlayed: List<AudioItem> = emptyList(),
        val recentlyAdded: List<AudioItem> = emptyList(),
        val albums: List<Album> = emptyList(),
        val isRefreshing: Boolean = false
    )
    
    private val _uiState = MutableStateFlow(DiscoverUiState(isLoading = true))
    val uiState: StateFlow<DiscoverUiState> = _uiState.asStateFlow()
    
    init {
        // Set up data observation
        loadData()
        
        // Only refresh library on first launch (when database is empty)
        viewModelScope.launch {
            val songs = musicRepository.getAllSongs().first()
            if (songs.isEmpty()) {
                musicRepository.refreshLibrary()
            }
            // isLoading will be set to false by loadData() when data arrives
        }
    }
    
    private fun loadData() {
        viewModelScope.launch {
            combine(
                musicRepository.getForYouTracks(limit = 20),
                musicRepository.getReconnectSongs(limit = 20),
                musicRepository.getMostPlayedSongs(limit = 20),
                musicRepository.getRecentlyAddedSongs(limit = 20),
                musicRepository.getTopAlbums(limit = 10)
            ) { forYou, reconnect, mostPlayed, recentlyAdded, topAlbums ->
                // Preserve the current isRefreshing state - don't overwrite it
                _uiState.value.copy(
                    isLoading = false,
                    forYou = forYou,
                    reconnect = reconnect,
                    mostPlayed = mostPlayed,
                    recentlyAdded = recentlyAdded,
                    albums = topAlbums
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }
    
    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            musicRepository.refreshLibrary()
            // Set isRefreshing to false AFTER refreshLibrary completes
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }
    
    fun playSong(song: AudioItem, queue: List<AudioItem>? = null) {
        val playQueue = queue ?: _uiState.value.forYou
        playbackStateManager.playSong(song, playQueue)
    }
}
