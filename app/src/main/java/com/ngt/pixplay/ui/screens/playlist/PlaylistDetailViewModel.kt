package com.ngt.pixplay.ui.screens.playlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ngt.pixplay.data.model.AudioItem
import com.ngt.pixplay.data.model.Playlist
import com.ngt.pixplay.data.repository.MusicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaylistDetailViewModel @Inject constructor(
    private val musicRepository: MusicRepository
) : ViewModel() {

    private val _playlist = MutableStateFlow<Playlist?>(null)
    val playlist: StateFlow<Playlist?> = _playlist.asStateFlow()

    private val _playlistSongs = MutableStateFlow<List<AudioItem>>(emptyList())
    val playlistSongs: StateFlow<List<AudioItem>> = _playlistSongs.asStateFlow()

    private var loadJob: kotlinx.coroutines.Job? = null

    fun loadPlaylist(playlistId: Long) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            val fetchedPlaylist = musicRepository.getPlaylistById(playlistId)
            _playlist.value = fetchedPlaylist
            
            if (fetchedPlaylist != null) {
                val songIds = fetchedPlaylist.getSongIdsList()
                musicRepository.getSongsByIds(songIds).collect {
                    _playlistSongs.value = it
                }
            }
        }
    }
    
    fun removeSong(songId: Long) {
        val currentPlaylist = _playlist.value ?: return
        viewModelScope.launch {
            musicRepository.removeSongFromPlaylist(currentPlaylist.id, songId)
            loadPlaylist(currentPlaylist.id) 
        }
    }

    fun deletePlaylist(playlistId: Long) {
        viewModelScope.launch {
            musicRepository.deletePlaylist(playlistId)
        }
    }

    fun renamePlaylist(playlistId: Long, newName: String) {
        viewModelScope.launch {
            musicRepository.renamePlaylist(playlistId, newName)
            loadPlaylist(playlistId) // Reload to update title
        }
    }
}
