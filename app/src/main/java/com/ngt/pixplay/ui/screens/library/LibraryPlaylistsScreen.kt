package com.ngt.pixplay.ui.screens.library

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ngt.pixplay.ui.components.CreatePlaylistDialog
import com.ngt.pixplay.ui.screens.playlist.PlaylistViewModel
import com.ngt.pixplay.data.model.Playlist
import com.ngt.pixplay.ui.components.RenamePlaylistDialog
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryPlaylistsScreen(
    onPlaylistClick: (Long) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlaylistViewModel = hiltViewModel()
) {
    val playlists by viewModel.playlists.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    var selectedPlaylistForActions by remember { mutableStateOf<Playlist?>(null) }
    var playlistToRename by remember { mutableStateOf<Playlist?>(null) }
    var playlistToDelete by remember { mutableStateOf<Playlist?>(null) }
    
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Playlists") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            val bottomPadding = com.ngt.pixplay.ui.common.LocalContentPadding.current.calculateBottomPadding()
            FloatingActionButton(
                modifier = Modifier.padding(bottom = bottomPadding),
                onClick = { showCreateDialog = true },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Playlist")
            }
        },
        contentWindowInsets = WindowInsets.navigationBars,
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { paddingValues ->
        if (playlists.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = paddingValues.calculateTopPadding()),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No playlists yet",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Create your first playlist",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            val bottomPadding = com.ngt.pixplay.ui.common.LocalContentPadding.current.calculateBottomPadding()

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = paddingValues.calculateTopPadding()),
                contentPadding = PaddingValues(
                    start = 16.dp, 
                    top = 16.dp, 
                    end = 16.dp, 
                    bottom = 16.dp + bottomPadding
                ),

                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(playlists) { playlist ->
                    com.ngt.pixplay.ui.components.PlaylistGridItem(
                        playlist = playlist,
                        onClick = { onPlaylistClick(playlist.id) },
                        modifier = Modifier.fillMaxWidth(),
                        onMoreClick = { selectedPlaylistForActions = playlist }
                    )
                }
            }
        }
        
        if (showCreateDialog) {
            CreatePlaylistDialog(
                onDismissRequest = { showCreateDialog = false },
                onConfirm = { name -> viewModel.createPlaylist(name) }
            )
        }

        if (selectedPlaylistForActions != null) {
            com.ngt.pixplay.ui.components.PlaylistOptionsBottomSheet(
                playlist = selectedPlaylistForActions!!,
                onDismissRequest = { selectedPlaylistForActions = null },
                onRename = { playlistToRename = selectedPlaylistForActions },
                onDelete = { playlistToDelete = selectedPlaylistForActions }
            )
        }

        if (playlistToRename != null) {
            RenamePlaylistDialog(
                initialName = playlistToRename!!.name,
                onDismissRequest = { playlistToRename = null },
                onConfirm = { newName ->
                    viewModel.renamePlaylist(playlistToRename!!.id, newName)
                    playlistToRename = null
                }
            )
        }

        if (playlistToDelete != null) {
            AlertDialog(
                onDismissRequest = { playlistToDelete = null },
                title = { Text("Delete Playlist") },
                text = { Text("Are you sure you want to delete '${playlistToDelete?.name}'?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            playlistToDelete?.let { viewModel.deletePlaylist(it.id) }
                            playlistToDelete = null
                        }
                    ) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { playlistToDelete = null }) {
                        Text("Cancel")
                    }
                }
            )
        }

    }
}
