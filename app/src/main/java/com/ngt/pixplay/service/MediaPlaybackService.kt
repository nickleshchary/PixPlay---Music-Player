package com.ngt.pixplay.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.media.audiofx.LoudnessEnhancer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.OptIn
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.CommandButton
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaController
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import androidx.media3.session.SessionToken
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.ngt.pixplay.MainActivity
import com.ngt.pixplay.R
import com.ngt.pixplay.data.model.AudioItem
import com.ngt.pixplay.data.preferences.SettingsDataStore
import com.ngt.pixplay.service.CoilBitmapLoader
import com.ngt.pixplay.service.PlaybackStateManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MediaPlaybackService : MediaSessionService(), Player.Listener {

    @Inject
    lateinit var playbackStateManager: PlaybackStateManager

    @Inject
    lateinit var player: ExoPlayer
    
    @Inject
    lateinit var settingsDataStore: SettingsDataStore

    private var mediaSession: MediaSession? = null
    
    // Audio normalization
    private var loudnessEnhancer: LoudnessEnhancer? = null

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    private val NOTIFICATION_ID = 888
    private val CHANNEL_ID = "pixplay_playback"

    // Custom Commands
    companion object {
        const val ACTION_FAVORITES = "ACTION_FAVORITES"
        const val ACTION_REPEAT = "ACTION_REPEAT"
        const val ACTION_SHUFFLE = "ACTION_SHUFFLE"
    }

    private val customCommandFavorites = SessionCommand(ACTION_FAVORITES, Bundle.EMPTY)
    private val customCommandRepeat = SessionCommand(ACTION_REPEAT, Bundle.EMPTY)
    private val customCommandShuffle = SessionCommand(ACTION_SHUFFLE, Bundle.EMPTY)

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()

        // 1. Manual Foreground Service Start
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                nm.createNotificationChannel(
                    NotificationChannel(
                        CHANNEL_ID,
                        "Playback",
                        NotificationManager.IMPORTANCE_LOW
                    )
                )
            }
            
            val pendingIntent = PendingIntent.getActivity(
                this,
                0,
                Intent(this, MainActivity::class.java),
                PendingIntent.FLAG_IMMUTABLE
            )

            val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("PixPlay")
                .setContentText("Ready to play")
                .setSmallIcon(R.drawable.ic_music_note)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build()

            startForeground(NOTIFICATION_ID, notification)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 2. Set MediaNotificationProvider
        setMediaNotificationProvider(
            DefaultMediaNotificationProvider(
                this,
                { NOTIFICATION_ID },
                CHANNEL_ID,
                R.string.app_name
            ).apply {
                 setSmallIcon(R.drawable.ic_music_note)
            }
        )

        // Player is now Injected (@Inject lateinit var player). 
        // We do NOT build a new one here.
        
        player.addListener(this)

        mediaSession = MediaSession.Builder(this, player)
            .setCallback(MediaSessionCallback())
            .setSessionActivity(
                PendingIntent.getActivity(
                    this,
                    0,
                    Intent(this, MainActivity::class.java),
                    PendingIntent.FLAG_IMMUTABLE
                )
            )
            .setBitmapLoader(CoilBitmapLoader(this, serviceScope))
            .build()
        
        // 3. Phantom Controller
        val sessionToken = SessionToken(this, ComponentName(this, MediaPlaybackService::class.java))
        val controllerFuture = MediaController.Builder(this, sessionToken).buildAsync()
        controllerFuture.addListener({ 
            try {
                controllerFuture.get() 
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(this))
        
        playbackStateManager.setService(this)

        serviceScope.launch {
            playbackStateManager.playbackState
                .map { it.currentSong }
                .distinctUntilChanged()
                .collectLatest { song ->
                    if (song != null) {
                        // Only update notification with favorite state. 
                        // Player media items are managed by playSong() / PlaybackStateManager.
                        updateNotification(song.isFavorite) 
                    }
                }
        }
        
        // Skip Silence setting observer
        serviceScope.launch {
            settingsDataStore.skipSilence.distinctUntilChanged().collect { enabled ->
                Log.d("MediaPlaybackService", "Skip silence enabled: $enabled")
                player.skipSilenceEnabled = enabled
            }
        }
        
        // Audio Normalization setting observer
        serviceScope.launch {
            settingsDataStore.audioNormalization.distinctUntilChanged().collect { enabled ->
                Log.d("MediaPlaybackService", "Audio normalization enabled: $enabled")
                if (enabled) {
                    setupLoudnessEnhancer()
                } else {
                    loudnessEnhancer?.enabled = false
                }
            }
        }
    }
    
    private fun setupLoudnessEnhancer() {
        try {
            val audioSessionId = player.audioSessionId
            if (audioSessionId != C.AUDIO_SESSION_ID_UNSET) {
                if (loudnessEnhancer == null) {
                    loudnessEnhancer = LoudnessEnhancer(audioSessionId)
                }
                // Apply moderate boost (500mb = +5dB) for consistent volume across tracks
                loudnessEnhancer?.setTargetGain(500)
                loudnessEnhancer?.enabled = true
                Log.d("MediaPlaybackService", "LoudnessEnhancer enabled with +5dB boost")
            }
        } catch (e: Exception) {
            Log.e("MediaPlaybackService", "Failed to setup LoudnessEnhancer", e)
        }
    }
    
    private fun releaseLoudnessEnhancer() {
        try {
            loudnessEnhancer?.release()
            loudnessEnhancer = null
        } catch (e: Exception) {
            Log.e("MediaPlaybackService", "Error releasing LoudnessEnhancer", e)
        }
    }

    fun playSong(song: AudioItem, queue: List<AudioItem>, index: Int) {
        val mediaItems = queue.map { audioItem ->
            val contentUri = ContentUris.withAppendedId(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                audioItem.id
            )
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

        player.setMediaItems(mediaItems, index, 0L)
        player.prepare()
        player.play()
        updateNotification()
    }

    fun togglePlayPause() {
        if (player.isPlaying) {
            player.pause()
        } else {
            if (player.playbackState == Player.STATE_IDLE) {
                player.prepare()
            }
            player.play()
        }
        updateNotification()
    }

    fun playNext() {
        if (player.hasNextMediaItem()) {
            player.seekToNextMediaItem()
        }
    }

    fun playPrevious() {
         if (player.currentPosition > 3000) {
            player.seekTo(0)
        } else if (player.hasPreviousMediaItem()) {
            player.seekToPreviousMediaItem()
        }
    }

    fun seekTo(position: Long) {
        player.seekTo(position)
    }

    override fun onDestroy() {
        playbackStateManager.setService(null)
        mediaSession?.run {
            // Do NOT release player here as it is a Singleton shared with PlaybackStateManager
            // player.release() 
            release()
            mediaSession = null
        }
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        updateNotification()
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        if (playbackState == Player.STATE_READY) {
            playbackStateManager.updateDuration(player.duration)
        }
        updateNotification()
    }
    
    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        playbackStateManager.updateCurrentIndex(player.currentMediaItemIndex)
        updateNotification()
    }

    override fun onRepeatModeChanged(repeatMode: Int) {
        updateNotification()
    }

    override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
        updateNotification()
    }
    
    private inner class MediaSessionCallback : MediaSession.Callback {
        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo
        ): MediaSession.ConnectionResult {
            val connectionResult = super.onConnect(session, controller)
            val sessionCommands = connectionResult.availableSessionCommands
                .buildUpon()
                .add(customCommandFavorites)
                .add(customCommandRepeat)
                .add(customCommandShuffle)
                .build()

            return MediaSession.ConnectionResult.accept(
                sessionCommands,
                connectionResult.availablePlayerCommands
            )
        }

        // IMPLEMENTED FIX: Set layout immediately on connection
        override fun onPostConnect(session: MediaSession, controller: MediaSession.ControllerInfo) {
            super.onPostConnect(session, controller)
            val buttons = getCustomLayoutButtons()
            if (buttons.isNotEmpty()) {
                mediaSession?.setCustomLayout(controller, buttons)
            }
        }

        override fun onCustomCommand(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            customCommand: SessionCommand,
            args: Bundle
        ): ListenableFuture<SessionResult> {
            when (customCommand.customAction) {
                ACTION_FAVORITES -> {
                    val newFavoriteState = playbackStateManager.toggleCurrentSongFavorite()
                    updateNotification(overrideFavoriteState = newFavoriteState)
                    return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
                }
                ACTION_REPEAT -> {
                    val newMode = when (player.repeatMode) {
                        Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
                        Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
                        else -> Player.REPEAT_MODE_OFF
                    }
                    player.repeatMode = newMode
                    return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
                }
                ACTION_SHUFFLE -> {
                    player.shuffleModeEnabled = !player.shuffleModeEnabled
                    return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
                }
            }
            return super.onCustomCommand(session, controller, customCommand, args)
        }
    }

    @OptIn(UnstableApi::class)
    @Suppress("DEPRECATION")
    private fun getCustomLayoutButtons(overrideFavoriteState: Boolean? = null): ImmutableList<CommandButton> {
        val isFavorite = overrideFavoriteState ?: (playbackStateManager.playbackState.value.currentSong?.isFavorite ?: false)
        
        val favoriteButton = CommandButton.Builder()
            .setDisplayName("Favorite")
            .setIconResId(if (isFavorite) R.drawable.ic_heart else R.drawable.ic_heart_outline)
            .setSessionCommand(customCommandFavorites)
            .build()
            
        val repeatButton = CommandButton.Builder()
             .setDisplayName("Repeat")
             .setIconResId(
                 when (player.repeatMode) {
                     Player.REPEAT_MODE_ONE -> R.drawable.ic_repeat_one
                     Player.REPEAT_MODE_ALL -> R.drawable.ic_repeat 
                     else -> R.drawable.ic_repeat 
                 }
             )
             .setSessionCommand(customCommandRepeat)
             .build()

        val shuffleButton = CommandButton.Builder()
            .setDisplayName("Shuffle")
            .setIconResId(if (player.shuffleModeEnabled) R.drawable.ic_shuffle_on else R.drawable.ic_shuffle)
            .setSessionCommand(customCommandShuffle)
            .build()
            
        return ImmutableList.of(favoriteButton, repeatButton, shuffleButton)
    }

    @OptIn(UnstableApi::class)
    @Suppress("DEPRECATION")
    private fun updateNotification(overrideFavoriteState: Boolean? = null) {
        if (mediaSession == null) return
        val buttons = getCustomLayoutButtons(overrideFavoriteState)
        mediaSession?.setCustomLayout(buttons)
    }
}
