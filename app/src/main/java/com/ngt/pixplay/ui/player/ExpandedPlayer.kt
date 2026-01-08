package com.ngt.pixplay.ui.player

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Subject
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.blur
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import coil3.compose.AsyncImage
import coil3.imageLoader
import coil3.request.allowHardware
import com.ngt.pixplay.data.model.AudioItem
import com.ngt.pixplay.ui.components.PlayerSlider
import com.ngt.pixplay.ui.components.SliderStyle
import com.ngt.pixplay.ui.components.Lyrics
import com.ngt.pixplay.ui.components.SleepTimerBottomSheet
import com.ngt.pixplay.ui.components.PlayerMenuBottomSheet
import com.ngt.pixplay.ui.components.QueueBottomSheet
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.collect

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun ExpandedPlayerContent(
    currentSong: AudioItem,
    queue: List<AudioItem>,
    currentSongIndex: Int,
    isPlaying: Boolean,
    position: Long,
    duration: Long,
    shuffleEnabled: Boolean,
    repeatMode: Int,
    onCollapseClick: () -> Unit,
    onPlayPauseClick: () -> Unit,
    onNextClick: () -> Unit,
    onPreviousClick: () -> Unit,

    onFavoriteClick: () -> Unit,
    onToggleLyrics: () -> Unit, // Add callback
    onSeek: (Long) -> Unit,
    onShuffleClick: () -> Unit,
    onRepeatClick: () -> Unit,
    sliderStyle: SliderStyle = SliderStyle.DEFAULT,
    // Lyrics settings
    showLyrics: Boolean = false,
    lyricsTextPosition: String = "CENTER",
    lyricsAnimationStyle: String = "APPLE",
    lyricsGlowEffect: Boolean = false,
    lyricsTextSize: Float = 24f,
    lyricsLineSpacing: Float = 1.3f,
    // Volume, Speed, Pitch controls
    currentVolume: Float = 1f,
    onVolumeChange: (Float) -> Unit = {},
    currentSpeed: Float = 1f,
    onSpeedChange: (Float) -> Unit = {},
    currentPitch: Float = 1f,
    onPitchChange: (Float) -> Unit = {},
    audioSessionId: Int = 0,
    onAddToPlaylist: () -> Unit = {},
    onQueueItemClick: (Int) -> Unit = {},
    onAlbumClick: () -> Unit = {},
    isDarkTheme: Boolean = false, // Add parameter
    isPureBlack: Boolean = false, // Add parameter
    seekIntervalSeconds: Int = 10, // Seek interval in seconds from settings
    swipeGesturesEnabled: Boolean = true, // Enable swipe to change tracks
    playerBackground: String = "solid", // "blur", "gradient", "solid"
    modifier: Modifier = Modifier
) {
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navigationBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    
    val context = androidx.compose.ui.platform.LocalContext.current
    
    // Capture fallback color
    val fallbackColor = MaterialTheme.colorScheme.primary
    
    // State for extracted colors
    var seedColor by remember { mutableStateOf<androidx.compose.ui.graphics.Color?>(null) }
    var gradientColors by remember { mutableStateOf<List<androidx.compose.ui.graphics.Color>>(emptyList()) }
    
    // Extract colors from album art
    LaunchedEffect(currentSong.albumArtUri) {
        if (currentSong.albumArtUri != null) {
            val request = coil3.request.ImageRequest.Builder(context)
                .data(currentSong.albumArtUri)
                .allowHardware(false)
                .build()
            
            val result = context.imageLoader.execute(request)
            val bitmap = (result.image as? coil3.BitmapImage)?.bitmap
            
            if (bitmap != null) {
                // Seed color for dynamic theme (used in Solid mode)
                seedColor = com.ngt.pixplay.ui.theme.PlayerColorExtractor.extractSeedColor(
                    bitmap = bitmap,
                    fallbackColor = fallbackColor
                )
                
                // Gradient colors for Gradient mode
                val palette = androidx.palette.graphics.Palette.from(bitmap).generate()
                gradientColors = com.ngt.pixplay.ui.theme.PlayerColorExtractor.extractGradientColors(
                    palette = palette,
                    fallbackColor = fallbackColor.toArgb()
                )
            } else {
                seedColor = null
                gradientColors = emptyList()
            }
        } else {
            seedColor = null
            gradientColors = emptyList()
        }
    }
    
    // Generate dynamic color scheme (for Solid mode)
    val dynamicColorScheme = if (seedColor != null) {
        com.materialkolor.rememberDynamicColorScheme(
            seedColor = seedColor!!,
            isDark = isDarkTheme,
            isAmoled = isPureBlack
        )
    } else {
        MaterialTheme.colorScheme
    }
    
    var sliderPosition by remember { mutableStateOf<Float?>(null) }
    var isLyricsMaximized by remember { mutableStateOf(false) }
    
    // Sleep Timer & Menu State
    var showSleepTimerSheet by remember { mutableStateOf(false) }
    var sleepTimerEnabled by remember { mutableStateOf(false) }
    var showPlayerMenu by remember { mutableStateOf(false) }
    var showQueueSheet by remember { mutableStateOf(false) }
    
    // Double-tap seek state
    var lastTapTime by remember { mutableStateOf(0L) }
    var skipMultiplier by remember { mutableStateOf(1) }
    var showSeekEffect by remember { mutableStateOf(false) }
    var seekDirection by remember { mutableStateOf("") }
    val layoutDirection = LocalLayoutDirection.current
    
    // Wrap with dynamic theme
    // For Blur/Gradient, we want a dark theme overlay look (white text) regardless of system theme
    val themeToUse = if (playerBackground == "solid") {
        dynamicColorScheme
    } else {
        // Enforce dark theme for Blur/Gradient to ensure text is visible on dark overlay
        com.materialkolor.rememberDynamicColorScheme(
            seedColor = seedColor ?: fallbackColor,
            isDark = true,
            isAmoled = false
        )
    }
    
    MaterialTheme(colorScheme = themeToUse) {
        Box(modifier = modifier.fillMaxSize()) {
            // Background Layer
            when (playerBackground) {
                "blur" -> {
                    AsyncImage(
                        model = currentSong.albumArtUri,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .blur(100.dp)
                    )
                    Box(
                        modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f))
                    )
                }
                "gradient" -> {
                    if (gradientColors.isNotEmpty()) {
                         Box(
                            Modifier
                                .fillMaxSize()
                                .background(Brush.verticalGradient(
                                    colors = if (gradientColors.size >= 2) gradientColors else listOf(gradientColors.first(), Color.Black)
                                ))
                                .background(Color.Black.copy(alpha = 0.3f))
                        )
                    } else {
                        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface))
                    }
                }
                else -> { // "solid"
                     Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface))
                }
            }

            // Foreground Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = statusBarPadding, bottom = navigationBarPadding)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
        // Top bar with collapse and more options
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilledTonalIconButton(onClick = onCollapseClick) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Collapse"
                )
            }
            
            Text(
                text = "Now Playing",
                style = MaterialTheme.typography.titleMedium
            )
            
            FilledTonalIconButton(onClick = { showPlayerMenu = true }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More options"
                )
            }
        }
        
        // Content area (Art or Lyrics) - Takes all available space
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),  // Always use weight to fill available space
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = showLyrics,
                label = "LyricsTransition",
                transitionSpec = {
                    (fadeIn(animationSpec = tween(400)) + scaleIn(initialScale = 0.92f, animationSpec = tween(400))) togetherWith
                        (fadeOut(animationSpec = tween(400)) + scaleOut(targetScale = 0.92f, animationSpec = tween(400)))
                }
            ) { showLyricsState ->
                if (showLyricsState) {
                    // Lyrics View - fills the entire content area
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp)
                    ) {
                        Lyrics(
                            lyricsData = currentSong.lyrics,
                            currentTime = position,
                            textPosition = lyricsTextPosition,
                            animationStyle = lyricsAnimationStyle,
                            glowEffect = lyricsGlowEffect,
                            textSize = lyricsTextSize,
                            lineSpacing = lyricsLineSpacing,
                            modifier = Modifier.fillMaxSize(),
                            accentColor = MaterialTheme.colorScheme.primary,
                            onSeek = onSeek
                        )
                    }
                } else {
                    BoxWithConstraints(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        val containerWidth = maxWidth
                        val itemWidth = maxWidth
                        
                        // Carousel Data: Window of [Prev, Current, Next]
                        val windowItems = remember(currentSong, queue, currentSongIndex) {
                            val prev = if (currentSongIndex > 0) queue.getOrNull(currentSongIndex - 1) else null
                            val next = if (currentSongIndex < queue.size - 1) queue.getOrNull(currentSongIndex + 1) else null
                            android.util.Log.d("ExpandedPlayer", "Building window: queueSize=${queue.size}, currentIndex=$currentSongIndex, hasPrev=${prev != null}, hasNext=${next != null}")
                            listOfNotNull(prev, currentSong, next)
                        }
                        val windowCurrentIndex = windowItems.indexOf(currentSong)
                        android.util.Log.d("ExpandedPlayer", "Window: size=${windowItems.size}, currentPos=$windowCurrentIndex")

                        val listState = rememberLazyGridState()
                        
                        // Track scroll state for swipe detection (Metrolist approach)
                        val currentVisibleItem by remember { derivedStateOf { listState.firstVisibleItemIndex } }
                        val itemScrollOffset by remember { derivedStateOf { listState.firstVisibleItemScrollOffset } }
                        
                        // Scroll to current song when it changes
                        LaunchedEffect(currentSong) {
                             if (windowCurrentIndex >= 0) {
                                 try {
                                     listState.scrollToItem(windowCurrentIndex)
                                 } catch (e: Exception) {
                                     // Handle potential race condition
                                 }
                             }
                        }

                        // Swipe Logic - Metrolist approach: trigger when item snaps to center during scroll
                        LaunchedEffect(itemScrollOffset) {
                            android.util.Log.d("ExpandedPlayer", "Swipe check: offset=$itemScrollOffset, scrolling=${listState.isScrollInProgress}, enabled=$swipeGesturesEnabled, windowIndex=$windowCurrentIndex, visible=$currentVisibleItem")
                            
                            // Only trigger when:
                            // 1. Currently scrolling
                            // 2. Swipe is enabled
                            // 3. Item is centered (offset == 0)
                            // 4. Valid index
                            if (!listState.isScrollInProgress || 
                                !swipeGesturesEnabled || 
                                itemScrollOffset != 0 || 
                                windowCurrentIndex < 0) return@LaunchedEffect

                            android.util.Log.d("ExpandedPlayer", "Swipe conditions met! visible=$currentVisibleItem, window=$windowCurrentIndex")
                            
                            if (currentVisibleItem > windowCurrentIndex) {
                                android.util.Log.d("ExpandedPlayer", "SWIPING TO NEXT")
                                onNextClick()
                            } else if (currentVisibleItem < windowCurrentIndex) {
                                android.util.Log.d("ExpandedPlayer", "SWIPING TO PREVIOUS")
                                onPreviousClick()
                            }
                        }

                        val snapLayoutInfoProvider = remember(listState) {
                           ThumbnailSnapLayoutInfoProvider(listState, { layout, item -> layout / 2f - item / 2f })
                        }

                        LazyHorizontalGrid(
                            rows = GridCells.Fixed(1),
                            state = listState,
                            flingBehavior = rememberSnapFlingBehavior(snapLayoutInfoProvider),
                            userScrollEnabled = swipeGesturesEnabled,
                            modifier = Modifier
                                .width(containerWidth)
                                .aspectRatio(1f)
                        ) {
                            items(items = windowItems, key = { it.id }) { song ->
                                val isCurrentSong = song.id == currentSong.id
                                
                                Box(
                                    modifier = Modifier
                                        .width(itemWidth)
                                        .fillMaxHeight()
                                        .padding(horizontal = 8.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                ) {
                                    val (backgroundColor, contentColor) = com.ngt.pixplay.util.ColorUtils.getThemeColorForId(song.albumId ?: song.id, MaterialTheme.colorScheme)

                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(backgroundColor)
                                            .then(
                                                if (isCurrentSong) {
                                                    Modifier.pointerInput(seekIntervalSeconds) {
                                                        detectTapGestures(
                                                            onDoubleTap = { offset ->
                                                                val now = System.currentTimeMillis()
                                                                if (now - lastTapTime < 1000) {
                                                                    skipMultiplier++
                                                                } else {
                                                                    skipMultiplier = 1
                                                                }
                                                                lastTapTime = now
                                                                
                                                                val skipAmount = (seekIntervalSeconds * 1000L) * skipMultiplier
                                                                
                                                                // Left side = backward, right side = forward
                                                                val isLeftSide = offset.x < size.width / 2
                                                                
                                                                if (isLeftSide) {
                                                                    val newPosition = (position - skipAmount).coerceAtLeast(0)
                                                                    onSeek(newPosition)
                                                                    seekDirection = "-${skipAmount / 1000}s"
                                                                    showSeekEffect = true
                                                                } else {
                                                                    val newPosition = (position + skipAmount).coerceAtMost(duration)
                                                                    onSeek(newPosition)
                                                                    seekDirection = "+${skipAmount / 1000}s"
                                                                    showSeekEffect = true
                                                                }
                                                            }
                                                        )
                                                    }
                                                } else Modifier
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.MusicNote,
                                            contentDescription = null,
                                            tint = contentColor.copy(alpha = 0.4f),
                                            modifier = Modifier.fillMaxSize(0.5f)
                                        )

                                        AsyncImage(
                                            model = song.albumArtUri,
                                            contentDescription = "${song.album} album art",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    }

                                    // Seek effect overlay
                                    if (isCurrentSong) {
                                        androidx.compose.animation.AnimatedVisibility(
                                            visible = showSeekEffect,
                                            enter = androidx.compose.animation.fadeIn(),
                                            exit = androidx.compose.animation.fadeOut(),
                                            modifier = Modifier.align(Alignment.Center)
                                        ) {
                                            SeekEffectOverlay(seekDirection = seekDirection)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // Auto-hide seek effect
        LaunchedEffect(showSeekEffect) {
            if (showSeekEffect) {
                kotlinx.coroutines.delay(1000)
                showSeekEffect = false
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        // Row for Song Info + Actions (Maximize + Favorite) - ALWAYS VISIBLE
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
             // Song Title/Artist Column
            Column(
                modifier = Modifier.weight(1f)
            ) {
                AnimatedContent(
                    targetState = currentSong.title,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "title"
                ) { title ->
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.basicMarquee(
                            iterations = 1,
                            initialDelayMillis = 3000,
                            velocity = 30.dp
                        )
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                AnimatedContent(
                    targetState = currentSong.artist,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "artist"
                ) { artist ->
                    Text(
                        text = artist,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.basicMarquee(
                            iterations = 1,
                            initialDelayMillis = 3000,
                            velocity = 30.dp
                        )
                    )
                }
            }
            
            // Actions Row (Maximize + Favorite)
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Maximize Toggle
                if (showLyrics) {
                    FilledTonalIconButton(
                        onClick = { isLyricsMaximized = !isLyricsMaximized }
                    ) {
                        Icon(
                            imageVector = if (isLyricsMaximized) 
                                Icons.Default.FullscreenExit 
                            else 
                                Icons.Default.Fullscreen,
                            contentDescription = if (isLyricsMaximized) "Minimize Lyrics" else "Maximize Lyrics",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                }

                // Favorite Button
                FilledTonalIconButton(
                    onClick = onFavoriteClick
                ) {
                    Icon(
                        imageVector = if (currentSong.isFavorite) 
                            Icons.Filled.Favorite 
                        else 
                            Icons.Outlined.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (currentSong.isFavorite)
                            MaterialTheme.colorScheme.error
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                )
                }
            }
        }
        
        // Progress slider - ALWAYS VISIBLE (even when lyrics maximized)
        Column {
            Spacer(modifier = Modifier.height(16.dp))
            
            PlayerSlider(
                value = sliderPosition ?: position.toFloat(),
                onValueChange = { sliderPosition = it },
                onValueChangeFinished = {
                    sliderPosition?.let {
                        onSeek(it.toLong())
                    }
                    sliderPosition = null
                },
                valueRange = 0f..duration.toFloat().coerceAtLeast(1f),
                style = sliderStyle,
                isPlaying = isPlaying,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
                modifier = Modifier.fillMaxWidth()
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatTime(sliderPosition?.toLong() ?: position),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formatTime(duration),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // Playback controls - Hidden when lyrics are maximized
        AnimatedVisibility(
            visible = !isLyricsMaximized,
            enter = slideInVertically { it } + fadeIn(),
            exit = shrinkVertically() + slideOutVertically { it } + fadeOut()
        ) {
            Column {
                Spacer(modifier = Modifier.height(16.dp))
                
                // Playback controls with Spring Animation
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    val backInteractionSource = remember { MutableInteractionSource() }
                    val nextInteractionSource = remember { MutableInteractionSource() }
                    val playPauseInteractionSource = remember { MutableInteractionSource() }

                    val isPlayPausePressed by playPauseInteractionSource.collectIsPressedAsState()
                    val isBackPressed by backInteractionSource.collectIsPressedAsState()
                    val isNextPressed by nextInteractionSource.collectIsPressedAsState()

                    val playPauseWeight by animateFloatAsState(
                        targetValue = if (isPlayPausePressed) 1.9f else if (isBackPressed || isNextPressed) 1.1f else 1.3f,
                        animationSpec = spring(
                            dampingRatio = 0.6f,
                            stiffness = 500f
                        ),
                        label = "playPauseWeight"
                    )

                    val backButtonWeight by animateFloatAsState(
                        targetValue = if (isBackPressed) 0.65f else if (isPlayPausePressed) 0.35f else 0.45f,
                        animationSpec = spring(
                            dampingRatio = 0.6f,
                            stiffness = 500f
                        ),
                        label = "backButtonWeight"
                    )

                    val nextButtonWeight by animateFloatAsState(
                        targetValue = if (isNextPressed) 0.65f else if (isPlayPausePressed) 0.35f else 0.45f,
                        animationSpec = spring(
                            dampingRatio = 0.6f,
                            stiffness = 500f
                        ),
                        label = "nextButtonWeight"
                    )

                    // Previous button
                    FilledIconButton(
                        onClick = onPreviousClick,
                        enabled = true,
                        shape = RoundedCornerShape(50),
                        interactionSource = backInteractionSource,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        ),
                        modifier = Modifier
                            .height(68.dp)
                            .weight(backButtonWeight)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipPrevious,
                            contentDescription = "Previous",
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Play/Pause button
                    FilledIconButton(
                        onClick = onPlayPauseClick,
                        shape = RoundedCornerShape(50),
                        interactionSource = playPauseInteractionSource,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                        modifier = Modifier
                            .height(68.dp)
                            .weight(playPauseWeight)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Pause" else "Play",
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Next button
                    FilledIconButton(
                        onClick = onNextClick,
                        enabled = true,
                        shape = RoundedCornerShape(50),
                        interactionSource = nextInteractionSource,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        ),
                        modifier = Modifier
                            .height(68.dp)
                            .weight(nextButtonWeight)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Next",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Secondary Action Row
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Repeat
                        IconButton(onClick = onRepeatClick) {
                            Icon(
                                imageVector = when (repeatMode) {
                                    1 -> Icons.Default.RepeatOne  // REPEAT_MODE_ONE
                                    2 -> Icons.Default.Repeat     // REPEAT_MODE_ALL
                                    else -> Icons.Default.Repeat  // REPEAT_MODE_OFF
                                },
                                contentDescription = "Repeat",
                                tint = if (repeatMode != 0)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        // Lyrics
                        IconButton(onClick = onToggleLyrics) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Subject,
                                contentDescription = "Lyrics",
                                tint = if (showLyrics)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        // Sleep Timer
                        IconButton(onClick = { 
                            if (sleepTimerEnabled) {
                                sleepTimerEnabled = false
                            } else {
                                showSleepTimerSheet = true 
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.Nightlight,
                                contentDescription = "Sleep Timer",
                                tint = if (sleepTimerEnabled)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        // Add to Playlist
                        IconButton(onClick = onAddToPlaylist) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.PlaylistAdd,
                                contentDescription = "Add to Playlist",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        // Queue (Moved to menu) - REMOVED from here

                        
                        // Shuffle
                        IconButton(onClick = onShuffleClick) {
                            Icon(
                                imageVector = Icons.Default.Shuffle,
                                contentDescription = "Shuffle",
                                tint = if (shuffleEnabled)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
        
        // Bottom padding
        Spacer(modifier = Modifier.height(32.dp))
    }
        
        // Sleep Timer Bottom Sheet
        SleepTimerBottomSheet(
            isVisible = showSleepTimerSheet,
            onDismiss = { showSleepTimerSheet = false },
            onStartTimer = { minutes ->
                sleepTimerEnabled = true
                // TODO: Integrate with MusicService sleep timer
                // sleepTimer.start(minutes)
            },
            onEndOfSong = {
                sleepTimerEnabled = true
                // TODO: Integrate with MusicService sleep timer
                // sleepTimer.start(-1)
            }
        )
        
        // Player Menu Bottom Sheet
        PlayerMenuBottomSheet(
            currentSong = currentSong,
            isVisible = showPlayerMenu,
            onDismiss = { showPlayerMenu = false },
            currentVolume = currentVolume,
            onVolumeChange = onVolumeChange,
            currentSpeed = currentSpeed,
            onSpeedChange = onSpeedChange,
            currentPitch = currentPitch,
            onPitchChange = onPitchChange,
            audioSessionId = audioSessionId,
            onShowQueue = { showQueueSheet = true },
            onAddToPlaylist = onAddToPlaylist,
            onAlbumClick = onAlbumClick
        )
        
        // Queue Bottom Sheet
        if (showQueueSheet) {
            QueueBottomSheet(
                queue = queue,
                currentSong = currentSong,
                onSongClick = { index ->
                    onQueueItemClick(index)
                    showQueueSheet = false
                },
                onDismiss = { showQueueSheet = false }
            )
        }
    }
    }
}

private fun formatTime(milliseconds: Long): String {
    val totalSeconds = milliseconds / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    
    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%d:%02d", minutes, seconds)
    }
}

@Composable
private fun SeekEffectOverlay(
    seekDirection: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = seekDirection,
        color = androidx.compose.ui.graphics.Color.White,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        modifier = modifier
            .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
            .padding(8.dp)
    )
}
