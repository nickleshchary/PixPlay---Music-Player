package com.ngt.pixplay.service

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.ngt.pixplay.data.model.AudioItem
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.withContext
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import com.ngt.pixplay.data.model.PersistPlayerState
import com.ngt.pixplay.data.repository.MusicRepository
import com.ngt.pixplay.data.preferences.SettingsDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

/**
 * Manages current playback state and controls ExoPlayer for local audio files.
 * 
 * This class coordinates with [MediaPlaybackService] to provide media session
 * integration for Android system media controls (Quick Settings, lock screen, etc.).
 * 
 * For detailed documentation on Android Media Controls behavior and implementation,
 * see [MEDIA_CONTROLS.md](../../../../../../MEDIA_CONTROLS.md) at the project root.
 */
@Singleton
class PlaybackStateManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val player: ExoPlayer,
    private val musicRepository: MusicRepository,
    private val settingsDataStore: SettingsDataStore
) {
    
    companion object {
        private const val TAG = "PlaybackStateManager"
        private const val PERSISTENT_STATE_FILE = "player_state.ser"
    }
    
    data class PlaybackState(
        val currentSong: AudioItem? = null,
        val isPlaying: Boolean = false,
        val currentPosition: Long = 0,
        val duration: Long = 0,
        val queue: List<AudioItem> = emptyList(),
        val currentIndex: Int = -1,
        val shuffleEnabled: Boolean = false,
        val repeatMode: Int = REPEAT_MODE_OFF
    ) {
        companion object {
            const val REPEAT_MODE_OFF = 0
            const val REPEAT_MODE_ALL = 1
            const val REPEAT_MODE_ONE = 2
        }
    }
    
    private val _playbackState = MutableStateFlow(PlaybackState())
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var positionUpdateJob: Job? = null
    
    private var mediaService: MediaPlaybackService? = null
    
    fun setService(service: MediaPlaybackService?) {
        this.mediaService = service
        Log.d(TAG, "MediaPlaybackService set: $service")
    }
    
    init {
        setupPlayerListener()
        restoreState()
        startPeriodicSave()
    }
    
    private fun setupPlayerListener() {
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                Log.d(TAG, "onIsPlayingChanged: $isPlaying")
                _playbackState.value = _playbackState.value.copy(isPlaying = isPlaying)
                if (isPlaying) {
                    startPositionUpdates()
                } else {
                    stopPositionUpdates()
                }
            }
            
            override fun onPlaybackStateChanged(playbackState: Int) {
                Log.d(TAG, "onPlaybackStateChanged: $playbackState")
                when (playbackState) {
                    Player.STATE_READY -> {
                        val duration = player.duration.coerceAtLeast(0)
                        Log.d(TAG, "Player ready, duration: $duration")
                        _playbackState.value = _playbackState.value.copy(duration = duration)
                    }
                    Player.STATE_ENDED -> {
                        Log.d(TAG, "Playback ended")
                        handlePlaybackEnded()
                    }
                }
            }
            
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                Log.d(TAG, "onMediaItemTransition: ${mediaItem?.mediaId}, reason: $reason")
                val index = player.currentMediaItemIndex
                val queue = _playbackState.value.queue
                if (index in queue.indices) {
                    _playbackState.value = _playbackState.value.copy(
                        currentSong = queue[index],
                        currentIndex = index,
                        currentPosition = 0
                    )
                    currentSongPlayedRecorded = false // Reset for new song
                }
            }
            
            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                Log.e(TAG, "Player error: ${error.message}", error)
            }

            override fun onRepeatModeChanged(repeatMode: Int) {
                _playbackState.value = _playbackState.value.copy(repeatMode = when(repeatMode) {
                    Player.REPEAT_MODE_ALL -> PlaybackState.REPEAT_MODE_ALL
                    Player.REPEAT_MODE_ONE -> PlaybackState.REPEAT_MODE_ONE
                    else -> PlaybackState.REPEAT_MODE_OFF
                })
            }

            override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                _playbackState.value = _playbackState.value.copy(shuffleEnabled = shuffleModeEnabled)
            }
        })
    }
    
    private fun handlePlaybackEnded() {
        val state = _playbackState.value
        when (state.repeatMode) {
            PlaybackState.REPEAT_MODE_ONE -> {
                player.seekTo(0)
                player.play()
            }
            PlaybackState.REPEAT_MODE_ALL -> {
                playNext()
            }
            else -> {
                if (state.currentIndex < state.queue.size - 1) {
                    playNext()
                }
            }
        }
    }
    
    private var currentSongPlayedRecorded = false

    private fun startPositionUpdates() {
        stopPositionUpdates()
        positionUpdateJob = scope.launch {
            while (isActive) {
                val position = player.currentPosition.coerceAtLeast(0)
                _playbackState.value = _playbackState.value.copy(currentPosition = position)
                
                // Record play count if > 30s or > 50% played, and not yet recorded
                if (!currentSongPlayedRecorded && _playbackState.value.currentSong != null) {
                    val duration = _playbackState.value.duration
                    if (duration > 0 && (position > 30000 || position > duration / 2)) {
                        currentSongPlayedRecorded = true
                        _playbackState.value.currentSong?.let { song ->
                            Log.d(TAG, "Incrementing play count for: ${song.title}")
                            musicRepository.incrementPlayCount(song.id)
                        }
                    }
                }
                
                delay(200)
            }
        }
    }
    
    private fun stopPositionUpdates() {
        positionUpdateJob?.cancel()
        positionUpdateJob = null
    }
    
    /**
     * Update the current song's favorite status in the playback state
     */
    fun updateCurrentSongFavorite(isFavorite: Boolean) {
        val currentSong = _playbackState.value.currentSong ?: return
        val updatedSong = currentSong.copy(isFavorite = isFavorite)
        _playbackState.value = _playbackState.value.copy(currentSong = updatedSong)
        
        // Persist to database
        scope.launch(Dispatchers.IO) {
            try {
                musicRepository.toggleFavorite(currentSong.id, isFavorite)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to persist favorite state", e)
            }
        }
    }
    
    /**
     * Toggle the current song's favorite status and return the new state.
     * Used by MediaPlaybackService for the custom favorites command button.
     */
    fun toggleCurrentSongFavorite(): Boolean {
        val currentSong = _playbackState.value.currentSong ?: return false
        val newFavoriteState = !currentSong.isFavorite
        updateCurrentSongFavorite(newFavoriteState)
        return newFavoriteState
    }
    
    /**
     * Build content URI for audio file using MediaStore
     */
    private fun getAudioContentUri(audioId: Long): Uri {
        return ContentUris.withAppendedId(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            audioId
        )
    }
    

    /**
     * Play a song with the given queue
     */
    fun playSong(song: AudioItem, queue: List<AudioItem> = listOf(song)) {
        Log.d(TAG, "playSong: ${song.title}, queue size: ${queue.size}")
        
        val index = queue.indexOf(song).coerceAtLeast(0)
        
        // Update state first
        _playbackState.value = _playbackState.value.copy(
            currentSong = song,
            queue = queue,
            currentIndex = index,
            duration = song.duration,
            currentPosition = 0
        )
        currentSongPlayedRecorded = false // Reset for new song start
        
        if (mediaService != null) {
            Log.d(TAG, "Delegating playSong to MediaPlaybackService")
            mediaService?.playSong(song, queue, index)
            return
        }
        
        Log.w(TAG, "MediaPlaybackService not set! Fallback to local player control (Media controls may work incorectly)")
        
        try {
            // Build media items using content URI (proper way to access MediaStore audio)
            val mediaItems = queue.map { audioItem ->
                val contentUri = getAudioContentUri(audioItem.id)
                MediaItem.Builder()
                    .setUri(contentUri)
                    .setMediaId(audioItem.id.toString())
                    .setMediaMetadata(
                        MediaMetadata.Builder()
                            .setTitle(audioItem.title)
                            .setArtist(audioItem.artist)
                            .setAlbumTitle(audioItem.album)
                            .setArtworkUri(audioItem.albumArtUri?.let { Uri.parse(it) })
                            .build()
                    )
                    .build()
            }
            
            // Clear and set new playlist
            player.stop()
            player.clearMediaItems()
            player.setMediaItems(mediaItems, index, 0L)
            player.prepare()
            player.playWhenReady = true
            
            Log.d(TAG, "Player prepared and starting playback")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error playing song", e)
        }
    }

    fun addToQueue(song: AudioItem) {
        val currentQueue = _playbackState.value.queue.toMutableList()
        if (currentQueue.isEmpty()) {
            playSong(song)
            return
        }
        
        currentQueue.add(song)
        _playbackState.value = _playbackState.value.copy(queue = currentQueue)
        
        if (mediaService != null) {
            // Assuming service has this method or we can just access player through it?
            // Ideally service should have addToQueue. If not, accessing player directly here works 
            // because PlaybackStateManager holds reference to the SAME player instance (injected as singleton).
            // But we should try to keep logic consistent.
            // Let's just add to player directly.
        }
        
        try {
            val contentUri = getAudioContentUri(song.id)
            val mediaItem = MediaItem.Builder()
                .setUri(contentUri)
                .setMediaId(song.id.toString())
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(song.title)
                        .setArtist(song.artist)
                        .setAlbumTitle(song.album)
                        .setArtworkUri(song.albumArtUri?.let { Uri.parse(it) })
                        .build()
                )
                .build()
            player.addMediaItem(mediaItem)
        } catch (e: Exception) {
            Log.e(TAG, "Error adding to queue", e)
        }
    }
    
    fun togglePlayPause() {
        Log.d(TAG, "togglePlayPause, mediaService: $mediaService")
        if (mediaService != null) {
            mediaService?.togglePlayPause()
        } else {
            // Fallback
            if (player.isPlaying) {
                player.pause()
            } else {
                if (player.playbackState == Player.STATE_IDLE) {
                    player.prepare()
                }
                player.play()
            }
        }
    }
    
    fun playNext() {
        Log.d(TAG, "playNext")
        if (mediaService != null) {
            mediaService?.playNext()
        } else {
            // Fallback logic
            val currentIndex = _playbackState.value.currentIndex
            val queueSize = _playbackState.value.queue.size
            
            if (queueSize > 0 && player.hasNextMediaItem()) {
                player.seekToNextMediaItem()
            } else if (queueSize > 0) {
                if (_playbackState.value.repeatMode == PlaybackState.REPEAT_MODE_ALL) {
                    player.seekTo(0, 0)
                }
            }
        }
    }
    
    fun playPrevious() {
        Log.d(TAG, "playPrevious")
        if (mediaService != null) {
            mediaService?.playPrevious()
        } else {
            // Fallback logic
            if (player.currentPosition > 3000) {
                player.seekTo(0)
            } else if (player.hasPreviousMediaItem()) {
                player.seekToPreviousMediaItem()
            }
        }
    }
    
    fun seekTo(position: Long) {
        Log.d(TAG, "seekTo: $position")
        if (mediaService != null) {
            mediaService?.seekTo(position)
        } else {
            player.seekTo(position)
        }
        _playbackState.value = _playbackState.value.copy(currentPosition = position)
    }
    
    private var lastShuffleToggleTime = 0L

    fun toggleShuffle() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastShuffleToggleTime < 500) {
            Log.d(TAG, "toggleShuffle: Debouncing rapid toggle")
            return
        }
        lastShuffleToggleTime = currentTime

        val newShuffleEnabled = !_playbackState.value.shuffleEnabled
        Log.d(TAG, "toggleShuffle: $newShuffleEnabled")
        // No manual state update - let the listener handle it to avoid race/feedback
        player.shuffleModeEnabled = newShuffleEnabled
    }
    
    fun cycleRepeatMode() {
        val currentMode = _playbackState.value.repeatMode
        val nextMode = when (currentMode) {
            PlaybackState.REPEAT_MODE_OFF -> PlaybackState.REPEAT_MODE_ALL
            PlaybackState.REPEAT_MODE_ALL -> PlaybackState.REPEAT_MODE_ONE
            else -> PlaybackState.REPEAT_MODE_OFF
        }
        Log.d(TAG, "cycleRepeatMode: $currentMode -> $nextMode")
        _playbackState.value = _playbackState.value.copy(repeatMode = nextMode)
        
        player.repeatMode = when (nextMode) {
            PlaybackState.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ALL
            PlaybackState.REPEAT_MODE_ONE -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
    }
    
    // Legacy methods for compatibility
    fun updateIsPlaying(isPlaying: Boolean) {
        if (isPlaying) player.play() else player.pause()
    }
    
    fun updatePosition(position: Long) = seekTo(position)
    
    fun updateDuration(duration: Long) {
        _playbackState.value = _playbackState.value.copy(duration = duration)
    }
    
    fun updateCurrentSong(song: AudioItem?) {
        _playbackState.value = _playbackState.value.copy(currentSong = song)
    }
    
    fun updateQueue(queue: List<AudioItem>, currentIndex: Int = 0) {
        _playbackState.value = _playbackState.value.copy(
            queue = queue,
            currentIndex = currentIndex,
            currentSong = queue.getOrNull(currentIndex)
        )
    }
    
    fun updateCurrentIndex(index: Int) {
        val queue = _playbackState.value.queue
        if (index in queue.indices) {
            _playbackState.value = _playbackState.value.copy(
                currentIndex = index,
                currentSong = queue[index]
            )
            player.seekTo(index, 0)
        }
    }
    
    // ===== VOLUME, SPEED, PITCH CONTROLS =====
    
    /**
     * Set player volume (0.0 to 1.0)
     */
    fun setVolume(volume: Float) {
        Log.d(TAG, "setVolume: $volume")
        player.volume = volume.coerceIn(0f, 1f)
    }
    
    /**
     * Get current player volume
     */
    fun getVolume(): Float = player.volume
    
    /**
     * Set playback speed (0.25 to 2.0)
     */
    fun setPlaybackSpeed(speed: Float) {
        Log.d(TAG, "setPlaybackSpeed: $speed")
        val clampedSpeed = speed.coerceIn(0.25f, 2f)
        player.playbackParameters = player.playbackParameters.withSpeed(clampedSpeed)
    }
    
    /**
     * Get current playback speed
     */
    fun getPlaybackSpeed(): Float = player.playbackParameters.speed
    
    /**
     * Set playback pitch (0.5 to 2.0)
     */
    fun setPlaybackPitch(pitch: Float) {
        Log.d(TAG, "setPlaybackPitch: $pitch")
        val clampedPitch = pitch.coerceIn(0.5f, 2f)
        val currentSpeed = player.playbackParameters.speed
        player.playbackParameters = androidx.media3.common.PlaybackParameters(currentSpeed, clampedPitch)
    }
    
    /**
     * Get current playback pitch
     */
    fun getPlaybackPitch(): Float = player.playbackParameters.pitch
    
    /**
     * Set both speed and pitch at once
     */
    fun setPlaybackParameters(speed: Float, pitch: Float) {
        Log.d(TAG, "setPlaybackParameters: speed=$speed, pitch=$pitch")
        player.playbackParameters = androidx.media3.common.PlaybackParameters(
            speed.coerceIn(0.25f, 2f),
            pitch.coerceIn(0.5f, 2f)
        )
    }
    
    /**
     * Get audio session ID for equalizer
     */
    fun getAudioSessionId(): Int = player.audioSessionId
    
    // ===== PERSISTENCE =====
    
    private fun saveState() {
        scope.launch(Dispatchers.IO) {
            try {
                val state = _playbackState.value
                val persistState = com.ngt.pixplay.data.model.PersistPlayerState(
                    currentSongId = state.currentSong?.id,
                    currentIndex = state.currentIndex,
                    currentPosition = state.currentPosition,
                    queue = state.queue,
                    shuffleEnabled = state.shuffleEnabled,
                    repeatMode = state.repeatMode
                )
                
                context.filesDir.resolve(PERSISTENT_STATE_FILE).outputStream().use { fos ->
                    java.io.ObjectOutputStream(fos).use { oos ->
                        oos.writeObject(persistState)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error saving state", e)
            }
        }
    }
    
    private fun restoreState() {
        scope.launch(Dispatchers.IO) {
            // Check if persistent queue is enabled
            val isPersistentQueueEnabled = settingsDataStore.persistentQueue.first()
            if (!isPersistentQueueEnabled) {
                Log.d(TAG, "Persistent queue disabled, skipping restore")
                return@launch
            }
            
            try {
                val file = context.filesDir.resolve(PERSISTENT_STATE_FILE)
                if (!file.exists()) return@launch
                
                val persistState = file.inputStream().use { fis ->
                    java.io.ObjectInputStream(fis).use { ois ->
                        ois.readObject() as com.ngt.pixplay.data.model.PersistPlayerState
                    }
                }
                
                withContext(Dispatchers.Main) {
                    // Update state flow
                    val currentSong = persistState.queue.find { it.id == persistState.currentSongId }
                        ?: persistState.queue.getOrNull(persistState.currentIndex.coerceAtLeast(0))
                        
                    _playbackState.value = _playbackState.value.copy(
                        queue = persistState.queue,
                        currentSong = currentSong,
                        currentIndex = persistState.currentIndex,
                        currentPosition = persistState.currentPosition,
                        duration = currentSong?.duration ?: 0,
                        shuffleEnabled = persistState.shuffleEnabled,
                        repeatMode = persistState.repeatMode
                    )
                    
                    // Setup player
                    player.shuffleModeEnabled = persistState.shuffleEnabled
                    player.repeatMode = when (persistState.repeatMode) {
                        PlaybackState.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ALL
                        PlaybackState.REPEAT_MODE_ONE -> Player.REPEAT_MODE_ONE
                        else -> Player.REPEAT_MODE_OFF
                    }

                    if (persistState.queue.isNotEmpty()) {
                        try {
                            // Check for permissions first? 
                            // This might fail if we don't have permission to read the files anymore
                            // But usually we do if they are MediaStore URIs
                            
                            val mediaItems = persistState.queue.map { audioItem ->
                                val contentUri = getAudioContentUri(audioItem.id)
                                MediaItem.Builder()
                                    .setUri(contentUri)
                                    .setMediaId(audioItem.id.toString())
                                    .setMediaMetadata(
                                        MediaMetadata.Builder()
                                            .setTitle(audioItem.title)
                                            .setArtist(audioItem.artist)
                                            .setAlbumTitle(audioItem.album)
                                            .setArtworkUri(audioItem.albumArtUri?.let { Uri.parse(it) })
                                            .build()
                                    )
                                    .build()
                            }
                            
                            player.setMediaItems(mediaItems, persistState.currentIndex, persistState.currentPosition)
                            player.prepare()
                            player.pause() // Don't auto play on restore
                        } catch (e: Exception) {
                            Log.e(TAG, "Error restoring player items", e)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error restoring state", e)
                // file might be corrupted, delete it
                try { context.filesDir.resolve(PERSISTENT_STATE_FILE).delete() } catch (e: Exception) {}
            }
        }
    }
    
    private fun startPeriodicSave() {
        scope.launch {
            while (isActive) {
                delay(10000) // Save every 10 seconds
                // Check if persistent queue is enabled
                val isPersistentQueueEnabled = settingsDataStore.persistentQueue.first()
                if (!isPersistentQueueEnabled) {
                    continue
                }
                // Only save if queue is not empty
                if (_playbackState.value.queue.isNotEmpty()) {
                   saveState()
                }
            }
        }
    }
}
