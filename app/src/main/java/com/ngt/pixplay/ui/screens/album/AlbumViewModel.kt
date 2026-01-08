package com.ngt.pixplay.ui.screens.album

import androidx.lifecycle.SavedStateHandle
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
class AlbumViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val musicRepository: MusicRepository,
    private val playbackStateManager: PlaybackStateManager
) : ViewModel() {
    
    private val albumId: Long = savedStateHandle.get<Long>("albumId") ?: -1L
    
    val album: StateFlow<Album?> = musicRepository.getAllAlbums()
        .map { albums -> albums.find { it.id == albumId } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    
    val songs: StateFlow<List<AudioItem>> = musicRepository.getAllSongs()
        .map { songs -> songs.filter { it.albumId == albumId } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    fun toggleFavorite(songId: Long, isFavorite: Boolean) {
        viewModelScope.launch {
            musicRepository.toggleFavorite(songId, isFavorite)
        }
    }
    
    fun toggleAlbumFavorite(isFavorite: Boolean) {
        viewModelScope.launch {
            musicRepository.toggleAlbumFavorite(albumId, isFavorite)
        }
    }
    
    fun playSong(song: AudioItem) {
        // Play the song with album songs as the queue
        val albumSongs = songs.value
        playbackStateManager.playSong(song, albumSongs)
    }
}
