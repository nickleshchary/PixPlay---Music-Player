package com.ngt.pixplay.ui.screens.album

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import coil3.imageLoader
import coil3.request.allowHardware
import com.ngt.pixplay.data.model.AudioItem
import com.ngt.pixplay.ui.components.ContainedLoadingIndicator
import com.ngt.pixplay.ui.components.ExperimentalMaterial3ExpressiveApi
import com.ngt.pixplay.util.ColorUtils

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AlbumScreen(
    onBackClick: () -> Unit,
    onSongClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = false,
    isPureBlack: Boolean = false,
    viewModel: AlbumViewModel = hiltViewModel(),
) {
    val haptic = LocalHapticFeedback.current
    val context = androidx.compose.ui.platform.LocalContext.current
    // Removed local isSystemInDarkTheme() call
    
    val album by viewModel.album.collectAsState()
    val songs by viewModel.songs.collectAsState()
    
    // Capture fallback color for non-composable scope
    val fallbackColor = MaterialTheme.colorScheme.primary

    var inSelectMode by rememberSaveable { mutableStateOf(false) }
    val selection = rememberSaveable(
        saver = listSaver<MutableList<Long>, Long>(
            save = { it.toList() },
            restore = { it.toMutableStateList() }
        )
    ) { mutableStateListOf() }
    
    val onExitSelectionMode = {
        inSelectMode = false
        selection.clear()
    }
    
    if (inSelectMode) {
        BackHandler(onBack = onExitSelectionMode)
    }

    // State for extracted seed color
    var seedColor by remember { mutableStateOf<androidx.compose.ui.graphics.Color?>(null) }
    
    val currentColorScheme = MaterialTheme.colorScheme

    // Extract seed color from album art
    androidx.compose.runtime.LaunchedEffect(album?.albumArtUri) {
        val artUri = album?.albumArtUri
        if (artUri != null) {
            val request = coil3.request.ImageRequest.Builder(context)
                .data(artUri)
                .allowHardware(false)
                .build()
            
            val result = context.imageLoader.execute(request)
            val bitmap = (result.image as? coil3.BitmapImage)?.bitmap
            
            if (bitmap != null) {
                seedColor = com.ngt.pixplay.ui.theme.PlayerColorExtractor.extractSeedColor(
                    bitmap = bitmap,
                    fallbackColor = fallbackColor
                )
            } else {
                val (bgColor, _) = ColorUtils.getThemeColorForId(album?.id ?: 0L, currentColorScheme)
                seedColor = bgColor
            }
        } else {
            val (bgColor, _) = ColorUtils.getThemeColorForId(album?.id ?: 0L, currentColorScheme)
            seedColor = bgColor
        }
    }
    
    // Generate dynamic color scheme from seed color
    val dynamicColorScheme = if (seedColor != null) {
        com.materialkolor.rememberDynamicColorScheme(
            seedColor = seedColor!!,
            isDark = isDarkTheme,
            isAmoled = isPureBlack
        )
    } else {
        MaterialTheme.colorScheme
    }

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    
    // Wrap with dynamic MaterialTheme
    MaterialTheme(colorScheme = dynamicColorScheme) {
        Scaffold(
            topBar = {
                AlbumTopAppBar(
                    title = album?.name ?: "",
                    inSelectMode = inSelectMode,
                    selectionCount = selection.size,
                    totalCount = songs.size,
                    onBackClick = onBackClick,
                    onExitSelectionMode = onExitSelectionMode,
                    onSelectAll = {
                        if (selection.size == songs.size) {
                            selection.clear()
                        } else {
                            selection.clear()
                            selection.addAll(songs.map { it.id })
                        }
                    },
                    scrollBehavior = scrollBehavior
                )
            },
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    top = paddingValues.calculateTopPadding(),
                    bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 90.dp
                )
            ) {
                val currentAlbum = album
                if (currentAlbum != null && songs.isNotEmpty()) {
                    item(key = "album_header") {
                        AlbumHeader(
                            albumName = currentAlbum.name,
                            artistName = currentAlbum.artist,
                            albumArtUri = currentAlbum.albumArtUri,
                            year = currentAlbum.year,
                            songCount = songs.size,
                            totalDuration = songs.sumOf { it.duration },
                            isFavorite = currentAlbum.isFavorite,
                            onPlayClick = {
                                // Play all songs starting from the first
                                songs.firstOrNull()?.let { song ->
                                    viewModel.playSong(song)
                                }
                            },
                            onFavoriteClick = { 
                                viewModel.toggleAlbumFavorite(!currentAlbum.isFavorite)
                            },
                            onMoreClick = { /* TODO: Show album menu */ }
                        )
                    }

                    itemsIndexed(
                        items = songs,
                        key = { _, song -> song.id }
                    ) { index, song ->
                        val isSelected = selection.contains(song.id)
                        val onCheckedChange: (Boolean) -> Unit = {
                            if (it) {
                                selection.add(song.id)
                            } else {
                                selection.remove(song.id)
                            }
                        }

                        AlbumSongListItem(
                            song = song,
                            index = index + 1,
                            isSelected = isSelected,
                            inSelectMode = inSelectMode,
                            onCheckedChange = onCheckedChange,
                            onClick = {
                                if (inSelectMode) {
                                    onCheckedChange(!isSelected)
                                } else {
                                    viewModel.playSong(song)
                                }
                            },
                            onLongClick = {
                                if (!inSelectMode) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    inSelectMode = true
                                    onCheckedChange(true)
                                }
                            },
                            onMoreClick = { /* TODO: Show song menu */ },
                            modifier = Modifier.animateItem()
                        )
                    }
                } else {
                    item(key = "loading") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            ContainedLoadingIndicator()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AlbumHeader(
    albumName: String,
    artistName: String,
    albumArtUri: String?,
    year: Int?,
    songCount: Int,
    totalDuration: Long,
    isFavorite: Boolean,
    onPlayClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Album Thumbnail - Large centered with shadow (matching expanded player)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .aspectRatio(1f)
                .shadow(
                    elevation = 24.dp,
                    shape = RoundedCornerShape(24.dp),
                    spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                ),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                 // Fallback Icon
                Icon(
                    imageVector = Icons.Default.Album, 
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxSize(0.5f)
                )
                
                AsyncImage(
                    model = albumArtUri,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Album Name
        Text(
            text = albumName,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 32.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Artist Name
        Text(
            text = artistName,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Normal,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Metadata - Year first, then song count • duration
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Year
            if (year != null) {
                Text(
                    text = year.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            }

            // Song Count • Duration
            Text(
                text = buildString {
                    append("$songCount songs")
                    if (totalDuration > 0) {
                        append(" • ")
                        append(formatDuration(totalDuration))
                    }
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Action Buttons Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Like Button - Smaller secondary button
            Surface(
                onClick = onFavoriteClick,
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(48.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                        tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Play Button - Larger primary circular button
            Surface(
                onClick = onPlayClick,
                color = MaterialTheme.colorScheme.primary,
                shape = CircleShape,
                modifier = Modifier.size(72.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            // Menu Button - Smaller secondary button
            Surface(
                onClick = onMoreClick,
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(48.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More options",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AlbumTopAppBar(
    title: String,
    inSelectMode: Boolean,
    selectionCount: Int,
    totalCount: Int,
    onBackClick: () -> Unit,
    onExitSelectionMode: () -> Unit,
    onSelectAll: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior
) {
    TopAppBar(
        title = {
            if (inSelectMode) {
                Text("$selectionCount selected")
            } else {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        navigationIcon = {
            if (inSelectMode) {
                IconButton(onClick = onExitSelectionMode) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Exit selection"
                    )
                }
            } else {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
        },
        actions = {
            if (inSelectMode) {
                Checkbox(
                    checked = selectionCount == totalCount && selectionCount > 0,
                    onCheckedChange = { onSelectAll() }
                )
                IconButton(
                    enabled = selectionCount > 0,
                    onClick = { /* TODO: Show selection menu */ }
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More options"
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            scrolledContainerColor = MaterialTheme.colorScheme.surface
        ),
        scrollBehavior = scrollBehavior
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AlbumSongListItem(
    song: AudioItem,
    index: Int,
    isSelected: Boolean,
    inSelectMode: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ListItem(
        headlineContent = {
            Text(
                text = song.title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyLarge
            )
        },
        supportingContent = {
            Text(
                text = song.artist,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = index.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        trailingContent = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(0.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatDuration(song.duration),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                if (inSelectMode) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = onCheckedChange
                    )
                } else {
                    
                    IconButton(onClick = onMoreClick) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More options"
                        )
                    }
                }
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
    )
}

/**
 * Format duration in milliseconds to MM:SS or H:MM:SS format
 */
private fun formatDuration(durationMs: Long): String {
    val totalSeconds = durationMs / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    
    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%d:%02d", minutes, seconds)
    }
}
