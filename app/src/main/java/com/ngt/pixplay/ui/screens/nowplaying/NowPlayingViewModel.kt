package com.ngt.pixplay.ui.screens.nowplaying

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ngt.pixplay.data.repository.MusicRepository
import com.ngt.pixplay.service.PlaybackStateManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NowPlayingViewModel @Inject constructor(
    private val playbackStateManager: PlaybackStateManager,
    private val musicRepository: MusicRepository
) : ViewModel() {
    
    val playbackState = playbackStateManager.playbackState.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        PlaybackStateManager.PlaybackState()
    )
    
    fun togglePlayPause() {
        playbackStateManager.togglePlayPause()
    }
    
    fun playNext() {
        playbackStateManager.playNext()
    }
    
    fun playPrevious() {
        playbackStateManager.playPrevious()
    }
    
    fun seekTo(position: Long) {
        playbackStateManager.seekTo(position)
    }
    
    fun toggleShuffle() {
        playbackStateManager.toggleShuffle()
    }
    
    fun toggleRepeat() {
        playbackStateManager.cycleRepeatMode()
    }
    
    fun toggleFavorite() {
        val currentSong = playbackState.value.currentSong ?: return
        val newFavoriteStatus = !currentSong.isFavorite
        viewModelScope.launch {
            musicRepository.toggleFavorite(currentSong.id, newFavoriteStatus)
            // Update the current song in playback state for immediate UI feedback
            playbackStateManager.updateCurrentSongFavorite(newFavoriteStatus)
        }
    }
    
    // ===== VOLUME, SPEED, PITCH CONTROLS =====
    
    fun setVolume(volume: Float) {
        playbackStateManager.setVolume(volume)
    }
    
    fun getVolume(): Float = playbackStateManager.getVolume()
    
    fun setPlaybackSpeed(speed: Float) {
        playbackStateManager.setPlaybackSpeed(speed)
    }
    
    fun getPlaybackSpeed(): Float = playbackStateManager.getPlaybackSpeed()
    
    fun setPlaybackPitch(pitch: Float) {
        playbackStateManager.setPlaybackPitch(pitch)
    }
    
    fun getPlaybackPitch(): Float = playbackStateManager.getPlaybackPitch()
    
    fun getAudioSessionId(): Int = playbackStateManager.getAudioSessionId()
    
    fun playSong(songId: Long) {
        viewModelScope.launch {
            val song = musicRepository.getSongById(songId)
            if (song != null) {
                // For now, just play the single song. ideally we should check where it came from to set a queue.
                playbackStateManager.playSong(song)
            }
        }
    }
    
    fun shuffleAll() {
        viewModelScope.launch {
            val allSongs = musicRepository.getAllSongs().first()
            if (allSongs.isNotEmpty()) {
                val shuffledSongs = allSongs.shuffled()
                playbackStateManager.playSong(shuffledSongs.first(), shuffledSongs)
            }
        }
    }
    
    fun addToQueue(song: com.ngt.pixplay.data.model.AudioItem) {
        playbackStateManager.addToQueue(song)
    }
    
    fun skipToQueueItem(index: Int) {
        playbackStateManager.updateCurrentIndex(index)
    }
}
