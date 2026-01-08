package com.ngt.pixplay.ui.screens.discover

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.ngt.pixplay.ui.components.ContainedLoadingIndicator
import com.ngt.pixplay.ui.components.ExperimentalMaterial3ExpressiveApi
import com.ngt.pixplay.data.model.AudioItem
import com.ngt.pixplay.ui.components.HideOnScrollFAB

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DiscoverScreen(
    onSongClick: (Long) -> Unit,
    onAlbumClick: (Long) -> Unit,
    onSeeAllAlbumsClick: () -> Unit,
    onSeeAllSongsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onShuffleClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DiscoverViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    val pullToRefreshState = rememberPullToRefreshState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text("Discover")
                },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        contentWindowInsets = WindowInsets.navigationBars,
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { paddingValues ->
        
        // Show centered loader during initial load
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                val infiniteTransition = rememberInfiniteTransition(label = "initialLoading")
                val infiniteProgress by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1500, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "progress"
                )
                ContainedLoadingIndicator(
                    progress = { infiniteProgress }
                )
            }
        } else {
            PullToRefreshBox(
                isRefreshing = uiState.isRefreshing,
                onRefresh = { viewModel.refresh() },
                state = pullToRefreshState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = paddingValues.calculateTopPadding()),
                indicator = {
                    val infiniteTransition = rememberInfiniteTransition(label = "loading")
                    val infiniteProgress by infiniteTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1500, easing = LinearEasing),
                            repeatMode = RepeatMode.Restart
                        ),
                        label = "progress"
                    )

                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 16.dp)
                            .graphicsLayer {
                                val fraction = pullToRefreshState.distanceFraction
                                alpha = if (uiState.isRefreshing) 1f else fraction.coerceIn(0f, 1f)
                                scaleX = if (uiState.isRefreshing) 1f else fraction.coerceIn(0f, 1f)
                                scaleY = if (uiState.isRefreshing) 1f else fraction.coerceIn(0f, 1f)
                            }
                    ) {
                        ContainedLoadingIndicator(
                            progress = { 
                                if (uiState.isRefreshing) infiniteProgress 
                                else pullToRefreshState.distanceFraction 
                            }
                        )
                    }
                }
            ) {
                val bottomPadding = com.ngt.pixplay.ui.common.LocalContentPadding.current.calculateBottomPadding()
                val lazyListState = androidx.compose.foundation.lazy.rememberLazyListState()

                Box(modifier = Modifier.fillMaxSize()) {
                    LazyColumn(
                        state = lazyListState,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                translationY = pullToRefreshState.distanceFraction * 100.dp.toPx()
                            },
                        contentPadding = PaddingValues(
                            top = 16.dp, 
                            bottom = 16.dp + bottomPadding + 80.dp // Extra space for FAB
                        )
                    ) {
                    // Show shimmer during refresh
                    if (uiState.isRefreshing) {
                        item {
                            SectionHeader(title = "For You", onSeeAll = null)
                        }
                        item {
                            com.ngt.pixplay.ui.components.shimmer.ShimmerHost {
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    contentPadding = PaddingValues(horizontal = 16.dp),
                                    userScrollEnabled = false
                                ) {
                                    items(3) {
                                        com.ngt.pixplay.ui.components.shimmer.GridItemPlaceholder()
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                        item {
                            SectionHeader(title = "Reconnect", subtitle = "Rediscover old favorites", onSeeAll = null)
                        }
                        item {
                            com.ngt.pixplay.ui.components.shimmer.ShimmerHost {
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    contentPadding = PaddingValues(horizontal = 16.dp),
                                    userScrollEnabled = false
                                ) {
                                    items(3) {
                                        com.ngt.pixplay.ui.components.shimmer.GridItemPlaceholder()
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                        item {
                            SectionHeader(title = "Most Played", subtitle = "Your heavy rotation", onSeeAll = null)
                        }
                        item {
                            com.ngt.pixplay.ui.components.shimmer.ShimmerHost {
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    contentPadding = PaddingValues(horizontal = 16.dp),
                                    userScrollEnabled = false
                                ) {
                                    items(3) {
                                        com.ngt.pixplay.ui.components.shimmer.GridItemPlaceholder()
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    } else {
                        // 1. For You Section
                        if (uiState.forYou.isNotEmpty()) {
                            item {
                                SectionHeader(title = "For You", onSeeAll = null)
                            }
                            item {
                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(uiState.forYou) { song ->
                                        SongCard(
                                            song = song,
                                            onClick = { 
                                                viewModel.playSong(song, uiState.forYou)
                                            }
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(24.dp))
                            }
                        }

                        // 2. Reconnect Section
                        if (uiState.reconnect.isNotEmpty()) {
                            item {
                                SectionHeader(title = "Reconnect", subtitle = "Rediscover old favorites", onSeeAll = null)
                            }
                            item {
                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(uiState.reconnect) { song ->
                                        SongCard(
                                            song = song,
                                            onClick = { 
                                                viewModel.playSong(song, uiState.reconnect)
                                            }
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(24.dp))
                            }
                        }

                        // 3. Most Played Section
                        if (uiState.mostPlayed.isNotEmpty()) {
                            item {
                                SectionHeader(title = "Most Played", subtitle = "Your heavy rotation", onSeeAll = null)
                            }
                            item {
                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(uiState.mostPlayed) { song ->
                                        SongCard(
                                            song = song,
                                            onClick = { 
                                                viewModel.playSong(song, uiState.mostPlayed)
                                            }
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(24.dp))
                            }
                        }

                        // 4. Recently Added
                        if (uiState.recentlyAdded.isNotEmpty()) {
                            item {
                                SectionHeader(title = "Recently Added", onSeeAll = onSeeAllSongsClick)
                            }
                            item {
                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(uiState.recentlyAdded) { song ->
                                        SongCard(
                                            song = song,
                                            onClick = { 
                                                viewModel.playSong(song, uiState.recentlyAdded)
                                            }
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(24.dp))
                            }
                        }

                        // 5. Top Albums
                        if (uiState.albums.isNotEmpty()) {
                            item {
                                SectionHeader(title = "Top Albums", onSeeAll = onSeeAllAlbumsClick)
                            }
                            item {
                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(uiState.albums) { album ->
                                        AlbumCard(
                                            album = album,
                                            onClick = { onAlbumClick(album.id) }
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(24.dp))
                            }
                        }
                    }
                    } // End LazyColumn

                    // FAB overlay
                    HideOnScrollFAB(
                        visible = !uiState.isLoading && !uiState.isRefreshing,
                        lazyListState = lazyListState,
                        icon = Icons.Default.Casino,
                        contentDescription = "Shuffle All",
                        onClick = onShuffleClick
                    )
                }
            }
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    subtitle: String? = null,
    onSeeAll: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        if (onSeeAll != null) {
            TextButton(onClick = onSeeAll) {
                Text("See all")
            }
        }
    }
}

@Composable
fun SongCard(
    song: AudioItem,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(140.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(140.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            AsyncImage(
                model = song.albumArtUri,
                contentDescription = song.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            
            // Fallback icon if no album art
            if (song.albumArtUri == null) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    modifier = Modifier
                        .size(48.dp)
                        .align(Alignment.Center),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Favorite indicator
            if (song.isFavorite) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Favorite",
                    modifier = Modifier
                        .padding(8.dp)
                        .size(20.dp)
                        .align(Alignment.TopEnd),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = song.title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        
        Text(
            text = song.artist,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun AlbumCard(
    album: com.ngt.pixplay.data.model.Album,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(140.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(140.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            AsyncImage(
                model = album.albumArtUri,
                contentDescription = album.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            
            // Fallback icon if no album art
            if (album.albumArtUri == null) {
                Icon(
                    imageVector = Icons.Default.Album,
                    contentDescription = null,
                    modifier = Modifier
                        .size(48.dp)
                        .align(Alignment.Center),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = album.name,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        
        Text(
            text = album.artist,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
