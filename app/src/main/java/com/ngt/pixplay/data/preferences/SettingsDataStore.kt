package com.ngt.pixplay.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/**
 * DataStore for app settings - offline-centric preferences
 */
@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // Appearance Settings
    object AppearanceKeys {
        val DARK_MODE = stringPreferencesKey("dark_mode") // "system", "light", "dark"
        val PURE_BLACK = booleanPreferencesKey("pure_black")
        val DYNAMIC_COLORS = booleanPreferencesKey("dynamic_colors")
        val GRID_ITEM_SIZE = stringPreferencesKey("grid_item_size") // "small", "medium", "large"
    }
    
    // Player Settings
    object PlayerKeys {
        val PERSISTENT_QUEUE = booleanPreferencesKey("persistent_queue")
        val SKIP_SILENCE = booleanPreferencesKey("skip_silence")
        val AUDIO_NORMALIZATION = booleanPreferencesKey("audio_normalization")
        val AUDIO_OFFLOAD = booleanPreferencesKey("audio_offload")
        val SEEK_INTERVAL = intPreferencesKey("seek_interval") // seconds
        val AUDIO_FLOAT_OUTPUT = booleanPreferencesKey("audio_float_output")
        
        // Provider Settings
        val ACTIVE_PROVIDER_TYPE = stringPreferencesKey("active_provider_type")
        val ACTIVE_PROVIDER_ID = longPreferencesKey("active_provider_id")
    }
    
    // Player UI Settings
    object PlayerUiKeys {
        val PLAYER_BACKGROUND = stringPreferencesKey("player_background") // "blur", "gradient", "solid"
        val SLIDER_STYLE = stringPreferencesKey("slider_style") // "default", "thick", "thin"
        val SWIPE_GESTURES = booleanPreferencesKey("swipe_gestures")
        val DOUBLE_TAP_SEEK = booleanPreferencesKey("double_tap_seek") // Double-tap on album art to seek
        val INCREMENTAL_SEEK = booleanPreferencesKey("incremental_seek") // Increase skip amount on rapid taps
        
        // Lyrics Settings
        val SHOW_LYRICS = booleanPreferencesKey("show_lyrics")
        val LYRICS_TEXT_POSITION = stringPreferencesKey("lyrics_text_position") // "LEFT", "CENTER", "RIGHT"
        val LYRICS_ANIMATION_STYLE = stringPreferencesKey("lyrics_animation_style") // "NONE", "FADE", "GLOW", "SLIDE", "KARAOKE", "APPLE"
        val LYRICS_GLOW_EFFECT = booleanPreferencesKey("lyrics_glow_effect")
        val LYRICS_TEXT_SIZE = floatPreferencesKey("lyrics_text_size")
        val LYRICS_LINE_SPACING = floatPreferencesKey("lyrics_line_spacing")
    }
    
    // Appearance
    val darkMode: Flow<String> = context.dataStore.data.map { it[AppearanceKeys.DARK_MODE] ?: "system" }
    val pureBlack: Flow<Boolean> = context.dataStore.data.map { it[AppearanceKeys.PURE_BLACK] ?: false }
    val dynamicColors: Flow<Boolean> = context.dataStore.data.map { it[AppearanceKeys.DYNAMIC_COLORS] ?: true }
    val gridItemSize: Flow<String> = context.dataStore.data.map { it[AppearanceKeys.GRID_ITEM_SIZE] ?: "medium" }
    
    // Player
    val persistentQueue: Flow<Boolean> = context.dataStore.data.map { it[PlayerKeys.PERSISTENT_QUEUE] ?: true }
    val skipSilence: Flow<Boolean> = context.dataStore.data.map { it[PlayerKeys.SKIP_SILENCE] ?: false }
    val audioNormalization: Flow<Boolean> = context.dataStore.data.map { it[PlayerKeys.AUDIO_NORMALIZATION] ?: false }
    val audioOffload: Flow<Boolean> = context.dataStore.data.map { it[PlayerKeys.AUDIO_OFFLOAD] ?: false }
    val seekInterval: Flow<Int> = context.dataStore.data.map { it[PlayerKeys.SEEK_INTERVAL] ?: 10 }
    val audioFloatOutput: Flow<Boolean> = context.dataStore.data.map { it[PlayerKeys.AUDIO_FLOAT_OUTPUT] ?: true }

    // Provider
    val activeProviderType: Flow<String> = context.dataStore.data.map { it[PlayerKeys.ACTIVE_PROVIDER_TYPE] ?: "MEDIASTORE" }
    val activeProviderId: Flow<Long> = context.dataStore.data.map { it[PlayerKeys.ACTIVE_PROVIDER_ID] ?: 0L }
    
    // Player UI
    val playerBackground: Flow<String> = context.dataStore.data.map { it[PlayerUiKeys.PLAYER_BACKGROUND] ?: "blur" }
    val sliderStyle: Flow<String> = context.dataStore.data.map { it[PlayerUiKeys.SLIDER_STYLE] ?: "default" }
    val swipeGestures: Flow<Boolean> = context.dataStore.data.map { it[PlayerUiKeys.SWIPE_GESTURES] ?: true }
    val doubleTapSeek: Flow<Boolean> = context.dataStore.data.map { it[PlayerUiKeys.DOUBLE_TAP_SEEK] ?: true }
    val incrementalSeek: Flow<Boolean> = context.dataStore.data.map { it[PlayerUiKeys.INCREMENTAL_SEEK] ?: true }
    
    // Lyrics
    val showLyrics: Flow<Boolean> = context.dataStore.data.map { it[PlayerUiKeys.SHOW_LYRICS] ?: false }
    val lyricsTextPosition: Flow<String> = context.dataStore.data.map { it[PlayerUiKeys.LYRICS_TEXT_POSITION] ?: "CENTER" }
    val lyricsAnimationStyle: Flow<String> = context.dataStore.data.map { it[PlayerUiKeys.LYRICS_ANIMATION_STYLE] ?: "APPLE" }
    val lyricsGlowEffect: Flow<Boolean> = context.dataStore.data.map { it[PlayerUiKeys.LYRICS_GLOW_EFFECT] ?: false }
    val lyricsTextSize: Flow<Float> = context.dataStore.data.map { it[PlayerUiKeys.LYRICS_TEXT_SIZE] ?: 24f }
    val lyricsLineSpacing: Flow<Float> = context.dataStore.data.map { it[PlayerUiKeys.LYRICS_LINE_SPACING] ?: 1.3f }
    
    // Setters
    suspend fun setDarkMode(value: String) {
        context.dataStore.edit { it[AppearanceKeys.DARK_MODE] = value }
    }
    
    suspend fun setPureBlack(value: Boolean) {
        context.dataStore.edit { it[AppearanceKeys.PURE_BLACK] = value }
    }
    
    suspend fun setDynamicColors(value: Boolean) {
        context.dataStore.edit { it[AppearanceKeys.DYNAMIC_COLORS] = value }
    }
    
    suspend fun setGridItemSize(value: String) {
        context.dataStore.edit { it[AppearanceKeys.GRID_ITEM_SIZE] = value }
    }
    
    suspend fun setPersistentQueue(value: Boolean) {
        context.dataStore.edit { it[PlayerKeys.PERSISTENT_QUEUE] = value }
    }
    
    suspend fun setSkipSilence(value: Boolean) {
        context.dataStore.edit { it[PlayerKeys.SKIP_SILENCE] = value }
    }
    
    suspend fun setAudioNormalization(value: Boolean) {
        context.dataStore.edit { it[PlayerKeys.AUDIO_NORMALIZATION] = value }
    }
    
    suspend fun setAudioOffload(value: Boolean) {
        context.dataStore.edit { it[PlayerKeys.AUDIO_OFFLOAD] = value }
    }
    
    suspend fun setSeekInterval(value: Int) {
        context.dataStore.edit { it[PlayerKeys.SEEK_INTERVAL] = value }
    }
    
    suspend fun setAudioFloatOutput(value: Boolean) {
        context.dataStore.edit { it[PlayerKeys.AUDIO_FLOAT_OUTPUT] = value }
    }

    suspend fun setActiveProvider(type: String, id: Long) {
        context.dataStore.edit { 
            it[PlayerKeys.ACTIVE_PROVIDER_TYPE] = type
            it[PlayerKeys.ACTIVE_PROVIDER_ID] = id
        }
    }
    
    suspend fun setPlayerBackground(value: String) {
        context.dataStore.edit { it[PlayerUiKeys.PLAYER_BACKGROUND] = value }
    }
    
    suspend fun setSliderStyle(value: String) {
        context.dataStore.edit { it[PlayerUiKeys.SLIDER_STYLE] = value }
    }
    
    suspend fun setSwipeGestures(value: Boolean) {
        context.dataStore.edit { it[PlayerUiKeys.SWIPE_GESTURES] = value }
    }
    
    suspend fun setDoubleTapSeek(value: Boolean) {
        context.dataStore.edit { it[PlayerUiKeys.DOUBLE_TAP_SEEK] = value }
    }
    
    suspend fun setIncrementalSeek(value: Boolean) {
        context.dataStore.edit { it[PlayerUiKeys.INCREMENTAL_SEEK] = value }
    }
    
    suspend fun setShowLyrics(value: Boolean) {
        context.dataStore.edit { it[PlayerUiKeys.SHOW_LYRICS] = value }
    }
    
    suspend fun setLyricsTextPosition(value: String) {
        context.dataStore.edit { it[PlayerUiKeys.LYRICS_TEXT_POSITION] = value }
    }
    
    suspend fun setLyricsAnimationStyle(value: String) {
        context.dataStore.edit { it[PlayerUiKeys.LYRICS_ANIMATION_STYLE] = value }
    }
    
    suspend fun setLyricsGlowEffect(value: Boolean) {
        context.dataStore.edit { it[PlayerUiKeys.LYRICS_GLOW_EFFECT] = value }
    }
    
    suspend fun setLyricsTextSize(value: Float) {
        context.dataStore.edit { it[PlayerUiKeys.LYRICS_TEXT_SIZE] = value }
    }
    
    suspend fun setLyricsLineSpacing(value: Float) {
        context.dataStore.edit { it[PlayerUiKeys.LYRICS_LINE_SPACING] = value }
    }
}
