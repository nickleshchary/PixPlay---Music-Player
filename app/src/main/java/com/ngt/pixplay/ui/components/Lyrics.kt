package com.ngt.pixplay.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.abs
import com.ngt.pixplay.ui.utils.fadingEdge

data class LyricsLine(
    val timestamp: Long,
    val text: String
)

@Composable
fun Lyrics(
    lyricsData: String?,
    currentTime: Long,
    textPosition: String, // LEFT, CENTER, RIGHT
    animationStyle: String, // NONE, FADE, GLOW, SLIDE, KARAOKE, APPLE
    glowEffect: Boolean,
    textSize: Float, // sp
    lineSpacing: Float, // multiplier
    modifier: Modifier = Modifier,
    accentColor: Color = MaterialTheme.colorScheme.primary, // Material You accent color
    onSeek: (Long) -> Unit = {}
) {
    // Parse lyrics
    val lyrics = remember(lyricsData) {
        if (lyricsData.isNullOrBlank()) {
            emptyList()
        } else {
            parseLyrics(lyricsData)
        }
    }

    val isSynced = remember(lyricsData) {
        !lyricsData.isNullOrEmpty() && lyricsData.contains("[")
    }

    // Find current line
    var currentLineIndex by remember { mutableIntStateOf(-1) }
    
    LaunchedEffect(currentTime) {
        if (isSynced) {
            val index = lyrics.indexOfLast { it.timestamp <= currentTime }
            if (index != -1 && index != currentLineIndex) {
                currentLineIndex = index
            }
        }
    }

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // Professional animation states for smooth PixPlay-style transitions
    var isAnimating by remember { mutableStateOf(false) }
    var isAutoScrollEnabled by remember { mutableStateOf(true) }
    var previousLineIndex by remember { mutableIntStateOf(0) }

    // PixPlay-style smooth page scroll function
    suspend fun performSmoothPageScroll(targetIndex: Int, duration: Int = 1500) {
        if (isAnimating) return // Prevent multiple animations
        isAnimating = true
        try {
            val itemInfo = listState.layoutInfo.visibleItemsInfo.firstOrNull { it.index == targetIndex }
            if (itemInfo != null) {
                // Item is visible, animate directly to center without sudden jumps
                val viewportHeight = listState.layoutInfo.viewportEndOffset - listState.layoutInfo.viewportStartOffset
                val center = listState.layoutInfo.viewportStartOffset + (viewportHeight / 2)
                val itemCenter = itemInfo.offset + itemInfo.size / 2
                val offset = itemCenter - center
                if (abs(offset) > 10) {
                    listState.animateScrollBy(
                        value = offset.toFloat(),
                        animationSpec = tween(durationMillis = duration)
                    )
                }
            } else {
                // Item is not visible, scroll to it first without animation
                listState.scrollToItem(targetIndex)
            }
        } finally {
            isAnimating = false
        }
    }

    // NestedScrollConnection to detect user scroll
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                if (source == NestedScrollSource.UserInput) {
                    isAutoScrollEnabled = false
                }
                return Offset.Zero
            }

            override suspend fun onPostFling(
                consumed: Velocity,
                available: Velocity
            ): Velocity {
                isAutoScrollEnabled = false
                return super.onPostFling(consumed, available)
            }
        }
    }


    // Auto-scroll logic with smooth centering
    LaunchedEffect(currentLineIndex, isAutoScrollEnabled) {
        if (!isSynced) return@LaunchedEffect
        if (isAutoScrollEnabled && currentLineIndex >= 0 && currentLineIndex != previousLineIndex) {
            performSmoothPageScroll(currentLineIndex, 1500) // Smooth 1.5s scroll
        }
        previousLineIndex = currentLineIndex
    }

    val textAlign = when (textPosition) {
        "LEFT" -> TextAlign.Left
        "RIGHT" -> TextAlign.Right
        else -> TextAlign.Center
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (lyrics.isEmpty()) {
            Text(
                text = "No lyrics available",
                modifier = Modifier.align(Alignment.Center),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .fadingEdge(vertical = 64.dp)
                    .nestedScroll(nestedScrollConnection),
                horizontalAlignment = when(textPosition) {
                    "LEFT" -> Alignment.Start
                    "RIGHT" -> Alignment.End
                    else -> Alignment.CenterHorizontally
                }
            ) {
                item { Spacer(modifier = Modifier.height(150.dp)) }
                itemsIndexed(lyrics) { index, line ->
                    val isCurrentLine = index == currentLineIndex
                    
                    // Parse animation style - if glowEffect is enabled, override to GLOW
                    val style = if (glowEffect) {
                        LyricsAnimationStyle.GLOW
                    } else {
                        try {
                            LyricsAnimationStyle.valueOf(animationStyle.uppercase())
                        } catch (e: Exception) {
                            LyricsAnimationStyle.NONE
                        }
                    }
                    
                    // Style values with smooth animations
                    val targetAlpha = if (isCurrentLine) 1f else 0.35f
                    val alpha by animateFloatAsState(
                        targetValue = targetAlpha, 
                        animationSpec = tween(400),
                        label = "alpha"
                    )
                    
                    val scale by animateFloatAsState(
                        targetValue = if (isCurrentLine) 1.02f else 1f,
                        animationSpec = tween(400),
                        label = "scale"
                    )
                    
                    // Fill animation
                    val fillProgress = remember { Animatable(0f) }
                    val pulseProgress = remember { Animatable(0f) }
                    
                    // Determine if fill animation should run
                    val shouldUseFill = LyricsAnimations.usesFill(style)
                    val shouldUsePulse = LyricsAnimations.usesPulse(style)
                    
                    // Launch fill animation when this line becomes current
                    LaunchedEffect(isCurrentLine, style, glowEffect) {
                        if (isCurrentLine && shouldUseFill) {
                            fillProgress.snapTo(0f)
                            fillProgress.animateTo(
                                targetValue = 1f,
                                animationSpec = tween(
                                    durationMillis = LyricsAnimations.getFillDuration(style),
                                    easing = FastOutSlowInEasing
                                )
                            )
                        } else {
                            fillProgress.snapTo(0f)
                        }
                    }
                    
                    // Continuous pulse animation (only for styles that use it)
                    LaunchedEffect(isCurrentLine, style, glowEffect) {
                        if (isCurrentLine && shouldUsePulse) {
                            while (true) {
                                pulseProgress.animateTo(
                                    targetValue = 1f,
                                    animationSpec = tween(
                                        durationMillis = LyricsAnimations.getPulseDuration(style),
                                        easing = LinearEasing
                                    )
                                )
                                pulseProgress.snapTo(0f)
                            }
                        } else {
                            pulseProgress.snapTo(0f)
                        }
                    }
                    
                    // Get the appropriate text style based on animation style
                    val textStyle = when (style) {
                        LyricsAnimationStyle.NONE -> LyricsAnimations.noneStyle(
                            isCurrentLine = isCurrentLine,
                            accentColor = accentColor,
                            textSize = textSize,
                            lineSpacing = lineSpacing,
                            textAlign = textAlign
                        )
                        LyricsAnimationStyle.FADE -> LyricsAnimations.fadeStyle(
                            isCurrentLine = isCurrentLine,
                            accentColor = accentColor,
                            textSize = textSize,
                            lineSpacing = lineSpacing,
                            textAlign = textAlign,
                            animatedAlpha = alpha
                        )
                        LyricsAnimationStyle.GLOW -> LyricsAnimations.glowStyle(
                            isCurrentLine = isCurrentLine,
                            accentColor = accentColor,
                            textSize = textSize,
                            lineSpacing = lineSpacing,
                            textAlign = textAlign,
                            fillProgress = fillProgress.value,
                            pulseProgress = pulseProgress.value
                        )
                        LyricsAnimationStyle.SLIDE -> LyricsAnimations.slideStyle(
                            isCurrentLine = isCurrentLine,
                            accentColor = accentColor,
                            textSize = textSize,
                            lineSpacing = lineSpacing,
                            textAlign = textAlign,
                            slideProgress = fillProgress.value
                        )
                        LyricsAnimationStyle.KARAOKE -> LyricsAnimations.karaokeStyle(
                            isCurrentLine = isCurrentLine,
                            accentColor = accentColor,
                            textSize = textSize,
                            lineSpacing = lineSpacing,
                            textAlign = textAlign,
                            fillProgress = fillProgress.value
                        )
                        LyricsAnimationStyle.APPLE -> LyricsAnimations.appleStyle(
                            isCurrentLine = isCurrentLine,
                            accentColor = accentColor,
                            textSize = textSize,
                            lineSpacing = lineSpacing,
                            textAlign = textAlign,
                            fillProgress = fillProgress.value,
                            pulseProgress = pulseProgress.value
                        )
                    }
                    
                    // Calculate scale - use bounce for GLOW, regular for others
                    val finalScale = when {
                        style == LyricsAnimationStyle.GLOW && isCurrentLine -> 
                            LyricsAnimations.calculateBounceScale(fillProgress.value)
                        else -> scale
                    }

                    // Use gradient text for GLOW style, regular text for others
                    if (style == LyricsAnimationStyle.GLOW && isCurrentLine) {
                        // Create gradient text with AnnotatedString
                        val fill = fillProgress.value
                        val pulse = pulseProgress.value
                        val pulseEffect = (kotlin.math.sin(pulse * Math.PI.toFloat()) * 0.15f).coerceIn(0f, 0.15f)
                        val glowIntensity = (fill + pulseEffect).coerceIn(0f, 1.2f)
                        
                        val glowBrush = Brush.horizontalGradient(
                            0.0f to accentColor.copy(alpha = 0.3f),
                            (fill * 0.7f).coerceIn(0f, 1f) to accentColor.copy(alpha = 0.9f),
                            fill to accentColor,
                            (fill + 0.1f).coerceIn(0f, 1f) to accentColor.copy(alpha = 0.7f),
                            1.0f to accentColor.copy(alpha = if (fill >= 1f) 1f else 0.3f)
                        )
                        
                        val styledText = buildAnnotatedString {
                            withStyle(
                                style = SpanStyle(
                                    shadow = Shadow(
                                        color = accentColor.copy(alpha = 0.8f * glowIntensity),
                                        offset = Offset(0f, 0f),
                                        blurRadius = 28f * (1f + pulseEffect)
                                    ),
                                    brush = glowBrush
                                )
                            ) {
                                append(line.text)
                            }
                        }
                        
                        Text(
                            text = styledText,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = (8 * lineSpacing).dp, horizontal = 16.dp)
                                .graphicsLayer {
                                    scaleX = finalScale
                                    scaleY = finalScale
                                },
                            fontSize = textSize.sp,
                            textAlign = textAlign,
                            fontWeight = FontWeight.ExtraBold,
                            lineHeight = (textSize * lineSpacing).sp
                        )
                    } else {
                        Text(
                            text = line.text,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = (8 * lineSpacing).dp, horizontal = 16.dp)
                                .graphicsLayer {
                                    scaleX = finalScale
                                    scaleY = finalScale
                                    this.alpha = if (style == LyricsAnimationStyle.FADE) 1f else alpha
                                }
                                .clickable { 
                                     if (isSynced) {
                                         onSeek(line.timestamp)
                                         // Temporarily disable auto-scroll to allow user to view the clicked line
                                         // It will re-enable if they hit re-sync or if we add a timeout (optional)
                                         // For now, PixPlay behavior is to just seek. 
                                         // We also want to smooth scroll to this item if it's not perfectly centered?
                                         // Actually PixPlay just seeks. The player will update position, 
                                         // which might trigger auto-scroll if we don't disable it.
                                         // But usually when manually seeking we might want to pause auto-scroll?
                                         // Let's keep auto-scroll enabled if they click, so it follows the song.
                                         // UNLESS they scrolled manually before clicking.
                                         
                                         // PixPlay logic:
                                         // if (isSynced && changeLyrics) {
                                         //    playerConnection.player.seekTo(item.time)
                                         //    ... smooth scroll to item ...
                                         //    lastPreviewTime = 0L
                                         // }
                                         
                                         // Our logic:
                                         scope.launch {
                                             performSmoothPageScroll(index, 1500)
                                         }
                                         // Ensure auto-scroll is ON so it follows the new position
                                         isAutoScrollEnabled = true
                                     }
                                },
                            style = textStyle
                        )
                    }
                }
                item { Spacer(modifier = Modifier.height(150.dp)) }
            }
            
            // Re-sync Button with smooth appear/disappear animation
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
                    .padding(horizontal = 24.dp)
            ) {
                AnimatedVisibility(
                    visible = !isAutoScrollEnabled && isSynced,
                    enter = slideInVertically { it } + fadeIn(),
                    exit = slideOutVertically { it } + fadeOut(),
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    FilledTonalButton(
                        onClick = {
                            scope.launch {
                                performSmoothPageScroll(currentLineIndex, 1500)
                            }
                            isAutoScrollEnabled = true
                        },
                        shape = CircleShape
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Re-sync",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Re-sync", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}

private fun parseLyrics(data: String): List<LyricsLine> {
    val lines = mutableListOf<LyricsLine>()
    val regex = Regex("\\[(\\d{2}):(\\d{2})\\.(\\d{2,3})\\](.*)")
    
    // Check if it's LRC format (contains timestamps)
    val isLrc = data.contains(regex)
    
    if (isLrc) {
        data.lines().forEach { line ->
            val match = regex.find(line)
            if (match != null) {
                val (min, sec, ms, content) = match.destructured
                val timestamp = min.toLong() * 60000 + sec.toLong() * 1000 + ms.padEnd(3, '0').take(3).toLong()
                if (content.isNotBlank()) {
                    lines.add(LyricsLine(timestamp, content.trim()))
                }
            }
        }
    } else {
        // Treat as plain text
        data.lines().filter { it.isNotBlank() }.forEachIndexed { index, content ->
           lines.add(LyricsLine(0, content.trim()))
        }
    }
    return lines
}
