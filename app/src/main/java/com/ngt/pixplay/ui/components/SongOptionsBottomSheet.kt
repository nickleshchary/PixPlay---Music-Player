package com.ngt.pixplay.ui.components

import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ngt.pixplay.data.model.AudioItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongOptionsBottomSheet(
    song: AudioItem,
    onDismissRequest: () -> Unit,
    onAddToQueue: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onGoToAlbum: () -> Unit,
    onGoToArtist: () -> Unit,
    onSongDetails: () -> Unit,
    onDelete: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = onDismissRequest,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            // Header with song info
            ListItem(
                headlineContent = { 
                    Text(
                        text = song.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    ) 
                },
                supportingContent = { 
                    Text(
                        text = song.artist,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    ) 
                },
                leadingContent = {
                    AsyncImage(
                        model = song.albumArtUri,
                        contentDescription = null,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                },
                colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
            )

            // Actions
            // Actions
            Column {
                ListItem(
                    headlineContent = { Text("Add to queue") },
                    leadingContent = { 
                        Icon(
                            Icons.AutoMirrored.Filled.QueueMusic, 
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        ) 
                    },
                    modifier = Modifier.clickable {
                        onAddToQueue()
                        onDismissRequest()
                    },
                    colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
                )
                
                ListItem(
                    headlineContent = { Text("Add to playlist") },
                    leadingContent = { 
                        Icon(
                            Icons.AutoMirrored.Filled.PlaylistAdd, 
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        ) 
                    },
                    modifier = Modifier.clickable {
                        onAddToPlaylist()
                        onDismissRequest()
                    },
                    colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
                )
                
                ListItem(
                    headlineContent = { Text("Go to album") },
                    supportingContent = { 
                        Text(
                            text = song.album, 
                            maxLines = 1, 
                            overflow = TextOverflow.Ellipsis
                        ) 
                    },
                    leadingContent = { 
                        Icon(
                            Icons.Default.Album, 
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        ) 
                    },
                    modifier = Modifier.clickable {
                        onGoToAlbum()
                        onDismissRequest()
                    },
                    colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
                )

                ListItem(
                    headlineContent = { Text("Go to artist") },
                    supportingContent = { 
                        Text(
                            text = song.artist, 
                            maxLines = 1, 
                            overflow = TextOverflow.Ellipsis
                        ) 
                    },
                    leadingContent = { 
                        Icon(
                            Icons.Default.Person, 
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        ) 
                    },
                    modifier = Modifier.clickable {
                        onGoToArtist()
                        onDismissRequest()
                    },
                    colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
                )
                
                ListItem(
                    headlineContent = { Text("Song details") },
                    supportingContent = { 
                        Text(
                            text = "View file info and metadata", 
                            maxLines = 1, 
                            overflow = TextOverflow.Ellipsis
                        ) 
                    },
                    leadingContent = { 
                        Icon(
                            Icons.Default.Info, 
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        ) 
                    },
                    modifier = Modifier.clickable {
                        onSongDetails()
                    },
                    colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
                )
                
                ListItem(
                    headlineContent = { 
                        Text(
                            "Delete from device",
                            color = MaterialTheme.colorScheme.error
                        ) 
                    },
                    leadingContent = { 
                        Icon(
                            Icons.Default.Delete, 
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        ) 
                    },
                    modifier = Modifier.clickable {
                        onDelete()
                        onDismissRequest()
                    },
                    colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
                )
            }
        }
    }
}
