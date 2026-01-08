package com.ngt.pixplay.ui.screens.library

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.HeartBroken
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ngt.pixplay.data.model.AudioItem
import com.ngt.pixplay.ui.components.SelectableSongListItem
import com.ngt.pixplay.ui.components.SongOptionsBottomSheet
import com.ngt.pixplay.ui.components.SongDetailsDialog
import com.ngt.pixplay.ui.components.AddToPlaylistSheet
import com.ngt.pixplay.ui.components.CreatePlaylistDialog
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryFavoritesScreen(
    onSongClick: (Long) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val favoriteSongs by viewModel.favoriteSongs.collectAsState()
    val playlists by viewModel.playlists.collectAsState()
    
    // Bottom sheet state
    var selectedSong by remember { mutableStateOf<AudioItem?>(null) }
    var showOptionsSheet by remember { mutableStateOf(false) }
    var showDetailsDialog by remember { mutableStateOf(false) }
    var showAddToPlaylistSheet by remember { mutableStateOf(false) }
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Liked Songs") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        contentWindowInsets = WindowInsets.navigationBars,
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { paddingValues ->
        val bottomPadding = com.ngt.pixplay.ui.common.LocalContentPadding.current.calculateBottomPadding()

        if (favoriteSongs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Column(
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.HeartBroken,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No liked songs yet",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Start liking songs to see them here",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = paddingValues.calculateTopPadding()),

                contentPadding = PaddingValues(bottom = bottomPadding)
            ) {
                items(favoriteSongs) { song ->
                    SelectableSongListItem(
                        song = song,
                        isSelected = false,
                        selectionMode = false,
                        onClick = {
                            viewModel.playSong(song)
                        },
                        onLongClick = { 
                            selectedSong = song
                            showOptionsSheet = true
                        },
                        onFavoriteClick = { 
                            viewModel.toggleFavorite(song.id, !song.isFavorite)
                        },
                        onMoreClick = {
                            selectedSong = song
                            showOptionsSheet = true
                        }
                    )
                }
            }
        }
    }
    
    // Song Options Bottom Sheet
    if (showOptionsSheet && selectedSong != null) {
        SongOptionsBottomSheet(
            song = selectedSong!!,
            onDismissRequest = { 
                showOptionsSheet = false
                if (!showDetailsDialog && !showAddToPlaylistSheet) {
                    selectedSong = null
                }
            },
            onAddToQueue = {
                viewModel.addToQueue(selectedSong!!)
                Toast.makeText(context, "Added to queue", Toast.LENGTH_SHORT).show()
            },
            onAddToPlaylist = {
                showAddToPlaylistSheet = true
            },
            onGoToAlbum = {
                // TODO: Navigate to album
            },
            onGoToArtist = {
                // TODO: Navigate to artist
            },
            onSongDetails = {
                showDetailsDialog = true
                showOptionsSheet = false
            },
            onDelete = {
                // Delete action
            }
        )
    }
    
    if (showAddToPlaylistSheet && selectedSong != null) {
        AddToPlaylistSheet(
            playlists = playlists,
            onPlaylistClick = { playlistId ->
                viewModel.addSongToPlaylist(playlistId, selectedSong!!.id)
                Toast.makeText(context, "Added to playlist", Toast.LENGTH_SHORT).show()
                showAddToPlaylistSheet = false
                selectedSong = null
            },
            onCreateNewClick = {
                showCreatePlaylistDialog = true
            },
            onDismissRequest = { 
                showAddToPlaylistSheet = false
                selectedSong = null
            }
        )
    }

    if (showCreatePlaylistDialog) {
        CreatePlaylistDialog(
            onDismissRequest = { showCreatePlaylistDialog = false },
            onConfirm = { name ->
                viewModel.createPlaylist(name)
                showCreatePlaylistDialog = false
            }
        )
    }
    
    // Song Details Dialog
    if (showDetailsDialog && selectedSong != null) {
        SongDetailsDialog(
            song = selectedSong!!,
            onDismiss = {
                showDetailsDialog = false
                selectedSong = null
            }
        )
    }
}
