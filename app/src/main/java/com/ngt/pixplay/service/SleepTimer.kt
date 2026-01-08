package com.ngt.pixplay.service

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.minutes

/**
 * Sleep Timer that can pause playback after a set duration or at the end of the current song.
 * 
 * Usage:
 * - Call start(minutes) to start a countdown timer
 * - Call start(-1) to pause at the end of the current song
 * - Call clear() to cancel the timer
 * - Observe isActive, triggerTime, and pauseWhenSongEnd for UI state
 */
class SleepTimer(
    private val scope: CoroutineScope,
    private val onTimerComplete: () -> Unit
) {
    private var sleepTimerJob: Job? = null
    
    var triggerTime by mutableLongStateOf(-1L)
        private set
    
    var pauseWhenSongEnd by mutableStateOf(false)
        private set
    
    val isActive: Boolean
        get() = triggerTime != -1L || pauseWhenSongEnd
    
    /**
     * Time remaining in milliseconds. Returns 0 if not active.
     */
    val timeRemaining: Long
        get() = if (triggerTime > 0) {
            (triggerTime - System.currentTimeMillis()).coerceAtLeast(0)
        } else {
            0
        }
    
    /**
     * Start the sleep timer.
     * @param minute The number of minutes until playback should pause.
     *               Pass -1 to pause at the end of the current song.
     */
    fun start(minute: Int) {
        sleepTimerJob?.cancel()
        sleepTimerJob = null
        
        if (minute == -1) {
            pauseWhenSongEnd = true
            triggerTime = -1L
        } else {
            pauseWhenSongEnd = false
            triggerTime = System.currentTimeMillis() + minute.minutes.inWholeMilliseconds
            sleepTimerJob = scope.launch {
                delay(minute.minutes)
                onTimerComplete()
                triggerTime = -1L
            }
        }
    }
    
    /**
     * Clear/cancel the sleep timer.
     */
    fun clear() {
        sleepTimerJob?.cancel()
        sleepTimerJob = null
        pauseWhenSongEnd = false
        triggerTime = -1L
    }
    
    /**
     * Call this when a song ends to trigger pause if pauseWhenSongEnd is enabled.
     */
    fun onSongEnded() {
        if (pauseWhenSongEnd) {
            pauseWhenSongEnd = false
            onTimerComplete()
        }
    }
    
    /**
     * Call this when transitioning to a new song to trigger pause if pauseWhenSongEnd is enabled.
     */
    fun onSongTransition() {
        if (pauseWhenSongEnd) {
            pauseWhenSongEnd = false
            onTimerComplete()
        }
    }
}
