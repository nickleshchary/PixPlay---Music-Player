package com.ngt.pixplay.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ngt.pixplay.data.preferences.SettingsDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {
    
    // Appearance
    val darkMode = settingsDataStore.darkMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "system")
    val pureBlack = settingsDataStore.pureBlack
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val dynamicColors = settingsDataStore.dynamicColors
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val gridItemSize = settingsDataStore.gridItemSize
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "medium")
    
    // Player
    val persistentQueue = settingsDataStore.persistentQueue
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val skipSilence = settingsDataStore.skipSilence
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val audioNormalization = settingsDataStore.audioNormalization
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val audioOffload = settingsDataStore.audioOffload
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val seekInterval = settingsDataStore.seekInterval
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 10)
    val audioFloatOutput = settingsDataStore.audioFloatOutput
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    
    // Player UI
    val playerBackground = settingsDataStore.playerBackground
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "blur")
    val sliderStyle = settingsDataStore.sliderStyle
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "default")
    val swipeGestures = settingsDataStore.swipeGestures
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val doubleTapSeek = settingsDataStore.doubleTapSeek
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val incrementalSeek = settingsDataStore.incrementalSeek
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
        
    // Lyrics
    val showLyrics = settingsDataStore.showLyrics
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val lyricsTextPosition = settingsDataStore.lyricsTextPosition
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "CENTER")
    val lyricsAnimationStyle = settingsDataStore.lyricsAnimationStyle
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "APPLE")
    val lyricsGlowEffect = settingsDataStore.lyricsGlowEffect
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val lyricsTextSize = settingsDataStore.lyricsTextSize
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 24f)
    val lyricsLineSpacing = settingsDataStore.lyricsLineSpacing
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1.3f)
    
    // Setters
    fun setDarkMode(value: String) = viewModelScope.launch { settingsDataStore.setDarkMode(value) }
    fun setPureBlack(value: Boolean) = viewModelScope.launch { settingsDataStore.setPureBlack(value) }
    fun setDynamicColors(value: Boolean) = viewModelScope.launch { settingsDataStore.setDynamicColors(value) }
    fun setGridItemSize(value: String) = viewModelScope.launch { settingsDataStore.setGridItemSize(value) }
    
    fun setPersistentQueue(value: Boolean) = viewModelScope.launch { settingsDataStore.setPersistentQueue(value) }
    fun setSkipSilence(value: Boolean) = viewModelScope.launch { settingsDataStore.setSkipSilence(value) }
    fun setAudioNormalization(value: Boolean) = viewModelScope.launch { settingsDataStore.setAudioNormalization(value) }
    fun setAudioOffload(value: Boolean) = viewModelScope.launch { settingsDataStore.setAudioOffload(value) }
    fun setSeekInterval(value: Int) = viewModelScope.launch { settingsDataStore.setSeekInterval(value) }
    fun setAudioFloatOutput(value: Boolean) = viewModelScope.launch { settingsDataStore.setAudioFloatOutput(value) }
    
    fun setPlayerBackground(value: String) = viewModelScope.launch { settingsDataStore.setPlayerBackground(value) }
    fun setSliderStyle(value: String) = viewModelScope.launch { settingsDataStore.setSliderStyle(value) }
    fun setSwipeGestures(value: Boolean) = viewModelScope.launch { settingsDataStore.setSwipeGestures(value) }
    fun setDoubleTapSeek(value: Boolean) = viewModelScope.launch { settingsDataStore.setDoubleTapSeek(value) }
    fun setIncrementalSeek(value: Boolean) = viewModelScope.launch { settingsDataStore.setIncrementalSeek(value) }
    
    fun setShowLyrics(value: Boolean) = viewModelScope.launch { settingsDataStore.setShowLyrics(value) }
    fun setLyricsTextPosition(value: String) = viewModelScope.launch { settingsDataStore.setLyricsTextPosition(value) }
    fun setLyricsAnimationStyle(value: String) = viewModelScope.launch { settingsDataStore.setLyricsAnimationStyle(value) }
    fun setLyricsGlowEffect(value: Boolean) = viewModelScope.launch { settingsDataStore.setLyricsGlowEffect(value) }
    fun setLyricsTextSize(value: Float) = viewModelScope.launch { settingsDataStore.setLyricsTextSize(value) }
    fun setLyricsLineSpacing(value: Float) = viewModelScope.launch { settingsDataStore.setLyricsLineSpacing(value) }
}
