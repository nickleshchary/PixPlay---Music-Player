# Media Controls Implementation Guide

This document describes the media playback control architecture in PixPlay, including Android system media controls integration (Quick Settings, lock screen, notification).

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                         UI Layer                                 │
├─────────────────────────────────────────────────────────────────┤
│  ExpandedPlayer.kt  │  NowPlayingScreen.kt  │  MiniPlayer.kt    │
│         ↓                    ↓                     ↓             │
│                    NowPlayingViewModel                           │
│                           ↓                                      │
├─────────────────────────────────────────────────────────────────┤
│                    PlaybackStateManager                          │
│         (Singleton - coordinates playback state)                 │
│                           ↓                                      │
├─────────────────────────────────────────────────────────────────┤
│                   MediaPlaybackService                           │
│      (MediaSessionService - handles system media controls)       │
│                           ↓                                      │
├─────────────────────────────────────────────────────────────────┤
│                      ExoPlayer                                   │
│              (Singleton - actual audio playback)                 │
└─────────────────────────────────────────────────────────────────┘
```

## Key Components

### PlaybackStateManager
**Location:** `service/PlaybackStateManager.kt`

The central coordinator for playback state. It:
- Manages the `PlaybackState` flow (current song, position, queue, etc.)
- Delegates playback commands to `MediaPlaybackService` when available
- Falls back to direct `ExoPlayer` control when service is unavailable
- Handles state persistence (saves/restores queue on app restart)

### MediaPlaybackService
**Location:** `service/MediaPlaybackService.kt`

Android `MediaSessionService` implementation that:
- Creates and manages the `MediaSession` for system integration
- Handles notification with playback controls
- Provides custom command buttons (Favorite, Repeat, Shuffle)
- Listens to player events and updates `PlaybackStateManager`

### ExoPlayer (Singleton)
**Location:** `di/MediaModule.kt`

Singleton ExoPlayer instance shared between `PlaybackStateManager` and `MediaPlaybackService`. This ensures consistent playback state across the app.

## Slider Seek Behavior

### Problem (Fixed)
When users drag the progress slider to seek to a different position, the song would pause/stop.

### Root Cause
The slider's `onValueChange` callback was calling `seekTo()` on **every** position change during dragging, which could interrupt playback.

### Solution
Use a local state variable to track slider position during dragging, and only call `seekTo()` when the user releases the slider:

```kotlin
// Local state for slider position during dragging
var sliderPosition by remember { mutableStateOf<Float?>(null) }

Slider(
    // Use sliderPosition while dragging, otherwise use actual position
    value = sliderPosition ?: position.toFloat(),
    
    // Only update local state during dragging (don't seek yet)
    onValueChange = { sliderPosition = it },
    
    // Seek only when user releases the slider
    onValueChangeFinished = {
        sliderPosition?.let { onSeek(it.toLong()) }
        sliderPosition = null
    },
    ...
)
```

### Implementation Locations
- **ExpandedPlayer.kt** (line ~539-547): Uses `PlayerSlider` component with proper pattern
- **NowPlayingScreen.kt** (line ~139-178): Uses standard `Slider` with the same pattern

## Playback Control Flow

### Play/Pause
```
UI Button → ViewModel.togglePlayPause() → PlaybackStateManager.togglePlayPause()
    → MediaPlaybackService.togglePlayPause() → ExoPlayer.play()/pause()
```

### Seek
```
Slider Release → ViewModel.seekTo(position) → PlaybackStateManager.seekTo(position)
    → MediaPlaybackService.seekTo(position) → ExoPlayer.seekTo(position)
```

### Next/Previous
```
UI Button → ViewModel.playNext()/playPrevious() → PlaybackStateManager.playNext()/playPrevious()
    → MediaPlaybackService.playNext()/playPrevious() → ExoPlayer.seekToNextMediaItem()/seekToPreviousMediaItem()
```

## System Media Controls

### Notification Controls
The notification displays:
- Album art
- Song title and artist
- Play/Pause button
- Previous/Next buttons
- Custom buttons: Favorite, Repeat, Shuffle

### Custom Command Buttons
Defined in `MediaPlaybackService`:
- `ACTION_FAVORITES` - Toggles favorite status
- `ACTION_REPEAT` - Cycles repeat mode (Off → All → One)
- `ACTION_SHUFFLE` - Toggles shuffle mode

### Lock Screen / Quick Settings
Android automatically provides lock screen and Quick Settings media controls through the `MediaSession`.

## State Flow

```kotlin
data class PlaybackState(
    val currentSong: AudioItem? = null,
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0,
    val duration: Long = 0,
    val queue: List<AudioItem> = emptyList(),
    val currentIndex: Int = -1,
    val shuffleEnabled: Boolean = false,
    val repeatMode: Int = REPEAT_MODE_OFF  // 0=Off, 1=All, 2=One
)
```

The state is exposed as a `StateFlow` and collected by UI components for reactive updates.

## Volume, Speed, and Pitch Controls

Available through `PlaybackStateManager`:
- `setVolume(volume: Float)` - 0.0 to 1.0
- `setPlaybackSpeed(speed: Float)` - 0.25 to 2.0
- `setPlaybackPitch(pitch: Float)` - 0.5 to 2.0

## Persistence

Player state is automatically saved every 10 seconds and restored on app restart:
- Current queue
- Current song and position
- Shuffle and repeat modes

File: `player_state.ser` in app's internal storage.

## Troubleshooting

### Media controls not appearing
1. Check that `MediaPlaybackService` is registered in `AndroidManifest.xml`
2. Verify notification channel is created
3. Ensure foreground service is started properly

### Seeking causes playback issues
1. Verify slider uses `onValueChangeFinished` pattern
2. Check that `seekTo()` is only called once per seek action

### Audio focus issues
ExoPlayer handles audio focus through `AudioAttributes` set in `MediaModule`.
