package com.ngt.pixplay.player

import android.content.Context
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.audio.DefaultAudioSink

private const val TAG = "PixPlayRenderersFactory"

/**
 * Custom RenderersFactory for PixPlay with 32-bit float output support.
 * 
 * Note: Audio offload is now configured at the ExoPlayer level via
 * TrackSelectionParameters.AudioOffloadPreferences, not in the AudioSink.
 * See MediaPlaybackService for offload configuration.
 */
@OptIn(UnstableApi::class)
class PixPlayRenderersFactory(
    context: Context,
    enableFloatOutput: Boolean = true
) : DefaultRenderersFactory(context) {

    init {
        Log.d(TAG, "Initializing with enableFloatOutput: $enableFloatOutput")
        setEnableAudioFloatOutput(enableFloatOutput)
        setExtensionRendererMode(EXTENSION_RENDERER_MODE_ON)
        Log.d(TAG, "Audio float output set to: $enableFloatOutput, Extension renderer mode: ON")
    }

    override fun buildAudioSink(
        context: Context,
        enableFloatOutput: Boolean,
        enableAudioTrackPlaybackParams: Boolean
    ): DefaultAudioSink {
        Log.d(TAG, "buildAudioSink called with enableFloatOutput: $enableFloatOutput")
        val sink = DefaultAudioSink.Builder(context)
            .setEnableFloatOutput(enableFloatOutput)
            .build()
        Log.d(TAG, "DefaultAudioSink built with float output: $enableFloatOutput")
        return sink
    }
}
