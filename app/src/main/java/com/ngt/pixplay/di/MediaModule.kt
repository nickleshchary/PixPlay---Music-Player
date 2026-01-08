package com.ngt.pixplay.di

import android.content.Context
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.TrackSelectionParameters
import androidx.media3.common.TrackSelectionParameters.AudioOffloadPreferences
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import com.ngt.pixplay.data.preferences.SettingsDataStore

private const val TAG = "MediaModule"

@Module
@InstallIn(SingletonComponent::class)
object MediaModule {
    
    @Provides
    @Singleton
    fun provideAudioAttributes(): AudioAttributes {
        Log.d(TAG, "provideAudioAttributes called")
        return AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()
    }
    

    @OptIn(UnstableApi::class)
    @Provides
    @Singleton
    fun provideExoPlayer(
        @ApplicationContext context: Context,
        audioAttributes: AudioAttributes,
        settingsDataStore: SettingsDataStore
    ): ExoPlayer {
        Log.d(TAG, "provideExoPlayer called - reading settings...")
        
        val enableFloatOutput = runBlocking { 
            val value = settingsDataStore.audioFloatOutput.first()
            Log.d(TAG, "Read audioFloatOutput from DataStore: $value")
            value
        }
        val enableAudioOffload = runBlocking { 
            val value = settingsDataStore.audioOffload.first()
            Log.d(TAG, "Read audioOffload from DataStore: $value")
            value
        }
        
        Log.d(TAG, "=== Audio Settings ===")
        Log.d(TAG, "32-bit Float Output setting: $enableFloatOutput")
        Log.d(TAG, "Audio Offload setting: $enableAudioOffload")
        
        // Float output and offload are mutually exclusive
        // If float output is enabled, offload must be disabled
        val actualFloatOutput = enableFloatOutput && !enableAudioOffload
        val actualOffload = enableAudioOffload && !enableFloatOutput
        
        Log.d(TAG, "Actual Float Output (after mutual exclusion): $actualFloatOutput")
        Log.d(TAG, "Actual Audio Offload (after mutual exclusion): $actualOffload")
        
        Log.d(TAG, "Creating PixPlayRenderersFactory...")
        val renderersFactory = com.ngt.pixplay.player.PixPlayRenderersFactory(context, actualFloatOutput)
        Log.d(TAG, "PixPlayRenderersFactory created with float output: $actualFloatOutput")
        
        Log.d(TAG, "Building ExoPlayer...")
        val player = ExoPlayer.Builder(context, renderersFactory)
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_LOCAL)
            .setAudioAttributes(audioAttributes, false) // false = app handles audio focus manually
            .build()
        Log.d(TAG, "ExoPlayer built successfully")
        
        // Only enable audio offload if float output is disabled
        if (actualOffload) {
            Log.d(TAG, "Configuring Audio Offload preferences...")
            val audioOffloadPreferences = AudioOffloadPreferences.Builder()
                .setAudioOffloadMode(AudioOffloadPreferences.AUDIO_OFFLOAD_MODE_ENABLED)
                .setIsGaplessSupportRequired(true)
                .setIsSpeedChangeSupportRequired(true)
                .build()
            
            player.trackSelectionParameters = player.trackSelectionParameters.buildUpon()
                .setAudioOffloadPreferences(audioOffloadPreferences)
                .build()
            
            Log.d(TAG, "Audio Offload ENABLED with gapless and speed change support")
        } else {
            Log.d(TAG, "Audio Offload DISABLED (Float output is enabled or offload setting is off)")
        }
        
        Log.d(TAG, "======================")
        Log.d(TAG, "ExoPlayer ready - Float: $actualFloatOutput, Offload: $actualOffload")
        
        return player
    }
}
