package com.ngt.pixplay.ui.screens.playlist

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ngt.pixplay.data.model.AudioItem
import com.ngt.pixplay.ui.components.SongListItem
import com.ngt.pixplay.ui.components.PlaylistHeader
import com.ngt.pixplay.ui.components.RenamePlaylistDialog
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import com.ngt.pixplay.data.model.Playlist

private fun formatDuration(durationMs: Long): String {
    val totalSeconds = durationMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    val hours = minutes / 60
    val remainingMinutes = minutes % 60
    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, remainingMinutes, seconds)
    } else {
        String.format("%d:%02d", minutes, seconds)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDetailScreen(
    playlistId: Long,
    onBackClick: () -> Unit,
    onSongClick: (Long) -> Unit,
    viewModel: PlaylistDetailViewModel = hiltViewModel()
) {
    val playlist by viewModel.playlist.collectAsState()
    val songs by viewModel.playlistSongs.collectAsState()
    
    var showMenuSheet by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(playlistId) {
        viewModel.loadPlaylist(playlistId)
    }

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(playlist?.name ?: "Playlist") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { paddingValues ->
        if (songs.isEmpty()) {
             Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text(
                    text = "No songs in this playlist",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                 item {
                     if (playlist != null) {
                         PlaylistHeader(
                             playlist = playlist!!,
                             songCount = songs.size,
                             durationText = formatDuration(songs.sumOf { it.duration }),
                             onPlayClick = { 
                                 // TODO: Play logic from first song
                                 onSongClick(songs.firstOrNull()?.id ?: -1L)
                             },
                             onShuffleClick = { 
                                 // TODO: Play shuffled
                             },
                             onMenuClick = { showMenuSheet = true }
                         )
                     }
                 }
                items(songs) { song ->
                    SongListItem(
                        song = song,
                        onClick = { onSongClick(song.id) },
                        onFavoriteClick = { /* TODO */ },
                        onMoreClick = { 
                             // Remove from playlist directly? Or menu?
                             // For now, assume remove.
                             viewModel.removeSong(song.id)
                        } 
                    )
                }
            }
        
            if (showMenuSheet && playlist != null) {
                com.ngt.pixplay.ui.components.PlaylistOptionsBottomSheet(
                    playlist = playlist!!,
                    onDismissRequest = { showMenuSheet = false },
                    onRename = { showRenameDialog = true },
                    onDelete = { showDeleteDialog = true }
                )
            }

            if (showRenameDialog && playlist != null) {
                RenamePlaylistDialog(
                    initialName = playlist!!.name,
                    onDismissRequest = { showRenameDialog = false },
                    onConfirm = { newName ->
                        viewModel.renamePlaylist(playlist!!.id, newName)
                        showRenameDialog = false
                    }
                )
            }

            if (showDeleteDialog && playlist != null) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    title = { Text("Delete Playlist") },
                    text = { Text("Are you sure you want to delete '${playlist!!.name}'?") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                viewModel.deletePlaylist(playlist!!.id)
                                showDeleteDialog = false
                                onBackClick()
                            }
                        ) {
                            Text("Delete", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}
