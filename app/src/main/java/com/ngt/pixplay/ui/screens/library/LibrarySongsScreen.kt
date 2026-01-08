package com.ngt.pixplay.ui.screens.library

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ngt.pixplay.ui.components.SelectableSongListItem
import com.ngt.pixplay.ui.components.SongDetailsDialog
import com.ngt.pixplay.ui.components.AddToPlaylistSheet
import com.ngt.pixplay.ui.components.CreatePlaylistDialog
import com.ngt.pixplay.ui.components.SelectionAppBar
import com.ngt.pixplay.data.model.AudioItem
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.content.ContentUris

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibrarySongsScreen(
    onSongClick: (Long) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val songs by viewModel.songs.collectAsState()
    val playlists by viewModel.playlists.collectAsState()
    val sortType by viewModel.sortType.collectAsState()
    
    // Selection state
    var selectedSongIds by rememberSaveable { mutableStateOf(setOf<Long>()) }
    val selectionMode = selectedSongIds.isNotEmpty()
    
    // Sort sheet state
    var showSortSheet by remember { mutableStateOf(false) }

    // Song Options Sheet State
    var songOptions by remember { mutableStateOf<AudioItem?>(null) }
    var showDetailsDialog by remember { mutableStateOf(false) }
    var showAddToPlaylistSheet by remember { mutableStateOf(false) }
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    
    Scaffold(
        topBar = {
            if (selectionMode) {
                SelectionAppBar(
                    selectedCount = selectedSongIds.size,
                    onClearSelection = { selectedSongIds = emptySet() },
                    onAddToPlaylist = { showAddToPlaylistSheet = true },
                    onDelete = { showDeleteDialog = true },
                    onShare = {
                        val uris = ArrayList<Uri>()
                        selectedSongIds.forEach { id ->
                            uris.add(ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id))
                        }
                        try {
                            val shareIntent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                                type = "audio/*"
                                putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share songs"))
                        } catch (e: Exception) {
                            Toast.makeText(context, "Failed to share songs", Toast.LENGTH_SHORT).show()
                        }
                    },
                    scrollBehavior = scrollBehavior
                )
            } else {
                TopAppBar(
                    title = { Text("Songs") },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = { showSortSheet = true }) {
                            Icon(Icons.Default.FilterList, contentDescription = "Sort")
                        }
                    },
                    scrollBehavior = scrollBehavior
                )
            }
        },
        contentWindowInsets = WindowInsets.navigationBars,
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { paddingValues ->
        val bottomPadding = com.ngt.pixplay.ui.common.LocalContentPadding.current.calculateBottomPadding()

        if (songs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = paddingValues.calculateTopPadding()),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Column(
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No songs found",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Your library is empty",
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
                items(songs) { song ->
                    val isSelected = selectedSongIds.contains(song.id)
                    SelectableSongListItem(
                        song = song,
                        isSelected = isSelected,
                        selectionMode = selectionMode,
                        onClick = {
                            if (selectionMode) {
                                selectedSongIds = if (isSelected) {
                                    selectedSongIds - song.id
                                } else {
                                    selectedSongIds + song.id
                                }
                            } else {
                                viewModel.playSong(song)
                            }
                        },
                        onLongClick = {
                            selectedSongIds = if (isSelected) {
                                selectedSongIds - song.id
                            } else {
                                selectedSongIds + song.id
                            }
                        },
                        onFavoriteClick = { 
                            viewModel.toggleFavorite(song.id, !song.isFavorite)
                        },
                        onMoreClick = { songOptions = song }
                    )
                }
            }
        }
    }
    
    if (showSortSheet) {
        com.ngt.pixplay.ui.components.SortOptionsSheet(
            sortType = sortType,
            onSortTypeChange = { viewModel.setSortType(it) },
            onDismissRequest = { showSortSheet = false }
        )
    }


    
    if (songOptions != null) {
        com.ngt.pixplay.ui.components.SongOptionsBottomSheet(
            song = songOptions!!,
            onDismissRequest = { 
                if (!showDetailsDialog && !showAddToPlaylistSheet) {
                    songOptions = null 
                }
            },
            onAddToQueue = { 
                viewModel.addToQueue(songOptions!!) 
                Toast.makeText(context, "Added to queue", Toast.LENGTH_SHORT).show()
            },
            onAddToPlaylist = { 
                showAddToPlaylistSheet = true
            },
            onGoToAlbum = { 
                // We need navigation callback for this deep link logic if not provided
                // Current signature doesn't support 'onNavigateToAlbum'.
                // Ideally passing these callbacks down.
            },
            onGoToArtist = {
            },
            onSongDetails = {
                showDetailsDialog = true
            },
            onDelete = { viewModel.deleteSong(songOptions!!) }
        )
    }

    if (showAddToPlaylistSheet && (songOptions != null || selectionMode)) {
        AddToPlaylistSheet(
            playlists = playlists,
            onPlaylistClick = { playlistId ->
                if (selectionMode) {
                   viewModel.addSongsToPlaylist(playlistId, selectedSongIds.toList())
                   Toast.makeText(context, "Added ${selectedSongIds.size} songs to playlist", Toast.LENGTH_SHORT).show()
                   selectedSongIds = emptySet() // Exit selection mode
                } else if (songOptions != null) {
                   viewModel.addSongToPlaylist(playlistId, songOptions!!.id) 
                   Toast.makeText(context, "Added to playlist", Toast.LENGTH_SHORT).show()
                   songOptions = null
                }
                showAddToPlaylistSheet = false
            },
            onCreateNewClick = {
                showCreatePlaylistDialog = true
            },
            onDismissRequest = { 
                showAddToPlaylistSheet = false
                if (!selectionMode) songOptions = null
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete songs") },
            text = { Text("Are you sure you want to delete ${selectedSongIds.size} songs?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val songsToDelete = songs.filter { selectedSongIds.contains(it.id) }
                        viewModel.deleteSongs(songsToDelete)
                        showDeleteDialog = false
                        selectedSongIds = emptySet()
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showCreatePlaylistDialog) {
        CreatePlaylistDialog(
            onDismissRequest = { showCreatePlaylistDialog = false },
            onConfirm = { name ->
                viewModel.createPlaylist(name)
                showCreatePlaylistDialog = false
                // Note: To automatically add song, we'd need the new playlist ID.
                // For simplified flow, just create and let user add again or assume success.
                // But ideally we should add it. For now, matching MainActivity logic.
            }
        )
    }

    if (showDetailsDialog && songOptions != null) {
        SongDetailsDialog(
            song = songOptions!!,
            onDismiss = { 
                showDetailsDialog = false 
                songOptions = null
            }
        )
    }

    // Permission handling for deletion
    // Permission handling for deletion
    val intentSenderLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        viewModel.onPermissionResult(result.resultCode)
    }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is LibraryViewModel.Event.RequestDeletePermission -> {
                    val request = androidx.activity.result.IntentSenderRequest.Builder(event.intentSender).build()
                    intentSenderLauncher.launch(request)
                }
                is LibraryViewModel.Event.ShowMessage -> {
                    android.widget.Toast.makeText(context, event.message, android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
