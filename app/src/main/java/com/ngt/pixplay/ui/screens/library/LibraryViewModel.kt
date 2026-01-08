package com.ngt.pixplay.ui.screens.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ngt.pixplay.data.model.Album
import com.ngt.pixplay.data.model.Artist
import com.ngt.pixplay.data.model.AudioItem
import com.ngt.pixplay.data.model.Playlist
import com.ngt.pixplay.data.repository.MusicRepository
import com.ngt.pixplay.service.PlaybackStateManager
import com.ngt.pixplay.ui.components.SortType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val musicRepository: MusicRepository,
    private val playbackStateManager: PlaybackStateManager,
    private val userPreferencesRepository: com.ngt.pixplay.data.repository.UserPreferencesRepository
) : ViewModel() {
    
    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()
    
    private val _sortType = MutableStateFlow(SortType.NAME_ASC)
    val sortType: StateFlow<SortType> = _sortType.asStateFlow()
    
    private val _rawSongs = musicRepository.getAllSongs()
    
    val songs: StateFlow<List<AudioItem>> = combine(
        _rawSongs,
        _sortType
    ) { songs, sortType ->
        when (sortType) {
            SortType.NAME_ASC -> songs.sortedBy { it.title.lowercase() }
            SortType.NAME_DESC -> songs.sortedByDescending { it.title.lowercase() }
            SortType.DATE_ADDED -> songs.sortedByDescending { it.dateAdded }
            SortType.DURATION -> songs.sortedByDescending { it.duration }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    val albums = musicRepository.getAllAlbums()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    val artists = musicRepository.getAllArtists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    val playlists = musicRepository.getAllPlaylists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        
    val favoriteSongs = musicRepository.getFavoriteSongs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val isGrid: StateFlow<Boolean> = userPreferencesRepository.isLibraryGrid
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    
    fun toggleView() {
        viewModelScope.launch {
            userPreferencesRepository.setLibraryGrid(!isGrid.value)
        }
    }

    fun selectTab(index: Int) {
        _selectedTab.value = index
    }
    
    fun setSortType(type: SortType) {
        _sortType.value = type
    }
    
    fun playSong(song: AudioItem) {
        // Play the song with the current song list as the queue
        val currentSongs = songs.value
        playbackStateManager.playSong(song, currentSongs)
    }
    
    fun toggleFavorite(songId: Long, isFavorite: Boolean) {
        viewModelScope.launch {
            musicRepository.toggleFavorite(songId, isFavorite)
        }
    }
    
    fun addToQueue(song: AudioItem) {
        // This usually appends to queue. PlaybackStateManager needs an 'addSong' method.
        // If not available, we can't do it easily without exposing queue content.
        // Assuming playbackStateManager has `addToQueue(song)`. 
        // If not, we might need to modify PlaybackStateManager.
        // Let's check typical PlaybackStateManager structure. 
        // If it uses MediaController, we can use transportControls.
        playbackStateManager.addToQueue(song)
    }
    
    // Deletion events
    sealed class Event {
        data class RequestDeletePermission(val intentSender: android.content.IntentSender) : Event()
        data class ShowMessage(val message: String) : Event()
    }
    
    private val _events = kotlinx.coroutines.channels.Channel<Event>(kotlinx.coroutines.channels.Channel.BUFFERED)
    val events = _events.receiveAsFlow()
    
    fun deleteSong(song: AudioItem) {
        viewModelScope.launch {
            try {
                // If it's a batch, logic is same but passed list
                val intentSender = musicRepository.deleteSong(song)
                if (intentSender != null) {
                    _events.send(Event.RequestDeletePermission(intentSender))
                } else {
                    _events.send(Event.ShowMessage("Song deleted"))
                    // Refresh not needed as Flow observes DB changes? 
                    // Repository deleted from DB? Yes.
                }
            } catch (e: Exception) {
               _events.send(Event.ShowMessage("Failed to delete: ${e.message}"))
            }
        }
    }
    
    fun onPermissionResult(resultCode: Int) {
        // If success, we might need to re-scan or remove from local list manually if not auto-updated?
        // Repository logic handled DB deletion on Pre-Q success.
        // On R+, if permission granted, the file is deleted by system? 
        // Actually, createDeleteRequest returns PendingIntent to *USER CONFIRM*. 
        // Once confirmed, system deletes it. 
        // So we need to ensure local DB is updated.
        // We really should listen to MediaStore changes or manually delete from DB after success.
        if (resultCode == android.app.Activity.RESULT_OK) {
             viewModelScope.launch {
                 musicRepository.refreshLibrary() // Safest bet to sync
                 _events.send(Event.ShowMessage("Deleted successfully"))
             }
        }
    }
    fun createPlaylist(name: String) {
        viewModelScope.launch {
            musicRepository.createPlaylist(name)
        }
    }

    fun addSongToPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch {
            musicRepository.addSongToPlaylist(playlistId, songId)
        }
    }

    fun addSongsToPlaylist(playlistId: Long, songIds: List<Long>) {
        viewModelScope.launch {
            musicRepository.addSongsToPlaylist(playlistId, songIds)
        }
    }

    fun deleteSongs(songs: List<AudioItem>) {
        viewModelScope.launch {
            try {
                val intentSender = musicRepository.deleteSongs(songs)
                if (intentSender != null) {
                    _events.send(Event.RequestDeletePermission(intentSender))
                } else {
                    _events.send(Event.ShowMessage("${songs.size} songs deleted"))
                }
            } catch (e: Exception) {
                _events.send(Event.ShowMessage("Failed to delete: ${e.message}"))
            }
        }
    }
}
