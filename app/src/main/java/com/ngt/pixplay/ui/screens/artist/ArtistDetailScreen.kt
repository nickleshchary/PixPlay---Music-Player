package com.ngt.pixplay.ui.screens.artist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.graphics.graphicsLayer
import coil3.compose.AsyncImage
import com.ngt.pixplay.data.model.Album
import com.ngt.pixplay.data.model.AudioItem
import com.ngt.pixplay.ui.common.LocalContentPadding
import com.ngt.pixplay.ui.utils.fadingEdge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistDetailScreen(
    onBackClick: () -> Unit,
    onAlbumClick: (Long) -> Unit,
    onSongClick: (Long) -> Unit, // Add callback
    viewModel: ArtistViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberLazyListState()
    val density = LocalDensity.current
    
    // Calculate header parallax and transparency logic
    // Using a simpler approach than PixPlay's rigid offset for now, but keeping the visual spirit
    val firstItemTranslationY by remember {
        derivedStateOf {
            if (scrollState.firstVisibleItemIndex == 0) {
                scrollState.firstVisibleItemScrollOffset.toFloat() * 0.5f // Parallax effect
            } else {
                0f
            }
        }
    }
    
    val showTopBarTitle by remember {
        derivedStateOf {
            scrollState.firstVisibleItemIndex > 0 || scrollState.firstVisibleItemScrollOffset > 600 // Show title after scrolling past header
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    AnimatedVisibility(
                        visible = showTopBarTitle,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Text(uiState.artist?.name ?: "") 
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (showTopBarTitle) MaterialTheme.colorScheme.surface else Color.Transparent,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface,
                    navigationIconContentColor = if (showTopBarTitle) MaterialTheme.colorScheme.onSurface else Color.White,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = if (showTopBarTitle) MaterialTheme.colorScheme.onSurface else Color.White
                )
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0) // Handle insets manually for parallax
    ) { paddingValues ->
        val bottomPadding = LocalContentPadding.current.calculateBottomPadding()

        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            val artist = uiState.artist
            
            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    state = scrollState,
                    contentPadding = PaddingValues(bottom = bottomPadding + 16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Header Section
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(380.dp) // Large header like PixPlay
                                .graphicsLayer {
                                    translationY = firstItemTranslationY
                                }
                        ) {
                            // Background Image
                            if (uiState.songs.isNotEmpty()) {
                                AsyncImage(
                                    model = uiState.songs.firstOrNull()?.albumArtUri, // Suggestion: Use artist image if available
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .fadingEdge(bottom = 120.dp), // Fade bottom into surface
                                    contentScale = ContentScale.Crop
                                )
                                
                                // Dark overlay for text readability
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(
                                                    Color.Black.copy(alpha = 0.3f), // Top for status bar
                                                    Color.Transparent,
                                                    Color.Black.copy(alpha = 0.8f)  // Bottom for text
                                                )
                                            )
                                        )
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(MaterialTheme.colorScheme.primaryContainer)
                                )
                            }

                            // Artist Info Overlay (Bottom Left)
                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = artist?.name ?: "Unknown Artist",
                                    style = MaterialTheme.typography.displaySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                // Stats Row
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${uiState.albums.size} Albums • ${uiState.songs.size} Songs",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                // Action Buttons - PixPlay style
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    // Play All
                                    FilledTonalButton(
                                        onClick = { 
                                            if (uiState.songs.isNotEmpty()) {
                                                viewModel.playSong(uiState.songs.first())
                                            }
                                        },
                                        colors = ButtonDefaults.filledTonalButtonColors(
                                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    ) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Play")
                                    }
                                    
                                    // Shuffle
                                    FilledTonalButton(
                                        onClick = { 
                                            if (uiState.songs.isNotEmpty()) {
                                                viewModel.playSong(uiState.songs.random())
                                            }
                                        },
                                        colors = ButtonDefaults.filledTonalButtonColors(
                                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                    ) {
                                        Icon(Icons.Default.Shuffle, contentDescription = null, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Shuffle")
                                    }
                                }
                            }
                        }
                    }

                    // Albums Section (Horizontal Scroll)
                    if (uiState.albums.isNotEmpty()) {
                        item {
                            Column(modifier = Modifier.padding(vertical = 16.dp)) {
                                Text(
                                    text = "Albums",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                                
                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(uiState.albums) { album ->
                                        ArtistAlbumCard(album = album, onClick = { onAlbumClick(album.id) })
                                    }
                                }
                            }
                        }
                    }

                    // Songs Section (Vertical List)
                    if (uiState.songs.isNotEmpty()) {
                        item {
                            Text(
                                text = "Songs",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                        
                        itemsIndexed(uiState.songs) { index, song ->
                            ArtistSongListItem(
                                song = song,
                                index = index + 1,
                                onClick = { 
                                    viewModel.playSong(song)
                                    onSongClick(song.id)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ArtistAlbumCard(
    album: Album,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(140.dp)
            .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = album.albumArtUri,
            contentDescription = album.name,
            modifier = Modifier
                .size(140.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentScale = ContentScale.Crop
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = album.name,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = "${album.songCount} songs",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ArtistSongListItem(
    song: AudioItem,
    index: Int,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = index.toString(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(32.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        
        AsyncImage(
            model = song.albumArtUri,
            contentDescription = null,
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentScale = ContentScale.Crop
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = song.album ?: "Unknown Album",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        
        IconButton(onClick = { /* Menu */ }) {
            Icon(Icons.Default.MoreVert, contentDescription = "More")
        }
    }
}
