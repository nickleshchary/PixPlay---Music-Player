package com.ngt.pixplay.ui.screens.artist

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ngt.pixplay.data.model.Album
import com.ngt.pixplay.data.model.Artist
import com.ngt.pixplay.data.model.AudioItem
import com.ngt.pixplay.data.repository.MusicRepository
import com.ngt.pixplay.service.PlaybackStateManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArtistViewModel @Inject constructor(
    private val musicRepository: MusicRepository,
    private val playbackStateManager: PlaybackStateManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val artistId: Long = checkNotNull(savedStateHandle["artistId"])

    data class ArtistUiState(
        val isLoading: Boolean = true,
        val artist: Artist? = null,
        val albums: List<Album> = emptyList(),
        val songs: List<AudioItem> = emptyList()
    )

    private val _uiState = MutableStateFlow(ArtistUiState())
    val uiState: StateFlow<ArtistUiState> = _uiState.asStateFlow()

    init {
        loadArtist()
    }

    private fun loadArtist() {
         viewModelScope.launch {
             musicRepository.getAllArtists()
                 .map { artists -> artists.find { it.id == artistId } }
                 .collect { artist ->
                     if (artist != null) {
                         // Group songs into albums
                         val albums = artist.songs.groupBy { it.albumId to it.album }
                             .map { (albumInfo, albumSongs) ->
                                 val (id, name) = albumInfo
                                 val firstSong = albumSongs.firstOrNull()
                                 Album(
                                     id = id ?: -1L,
                                     name = name,
                                     artist = artist.name,
                                     artistId = artist.id,
                                     albumArtUri = firstSong?.albumArtUri,
                                     songCount = albumSongs.size,
                                     songs = albumSongs,
                                     isFavorite = false // Default
                                 )
                             }
                             .sortedBy { it.name }

                         _uiState.value = ArtistUiState(
                             isLoading = false,
                             artist = artist,
                             albums = albums,
                             songs = artist.songs.sortedBy { it.title } // Sort alphabetically
                         )
                     } else {
                         _uiState.value = ArtistUiState(isLoading = false, artist = null)
                     }
                 }
         }
    }

    fun playSong(song: AudioItem) {
        val currentSongs = _uiState.value.songs
        if (currentSongs.isNotEmpty()) {
            playbackStateManager.playSong(song, currentSongs)
        }
    }
    
    fun playAlbum(album: Album) {
        val songs = album.songs
        if (songs.isNotEmpty()) {
             playbackStateManager.playSong(songs.first(), songs)
        }
    }
}
