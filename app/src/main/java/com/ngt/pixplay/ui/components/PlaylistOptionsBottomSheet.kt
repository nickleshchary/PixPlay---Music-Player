package com.ngt.pixplay.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ngt.pixplay.data.model.Playlist

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistOptionsBottomSheet(
    playlist: Playlist,
    onDismissRequest: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit
) {
    ModalBottomSheet(
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
            // Header
            ListItem(
                headlineContent = {
                    Text(
                        text = playlist.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                supportingContent = {
                    val count = playlist.getSongIdsList().size
                    Text(
                        text = "$count songs",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                leadingContent = {
                    Surface(
                        modifier = Modifier.size(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
            )
            
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))

            // Copy/Duplicate could be here too

            // Rename
            ListItem(
                headlineContent = { Text("Rename Playlist") },
                leadingContent = { 
                    Icon(
                        Icons.Default.Edit, 
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    ) 
                },
                modifier = Modifier.clickable {
                    onRename()
                    // onDismissRequest() is handled by caller usually if logic separate, 
                    // but here we want to close sheet THEN open dialog.
                    // The caller `onRename` will set `showDialog = true`.
                    // We must close sheet.
                    onDismissRequest() 
                },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
            )
            
            // Delete
            ListItem(
                headlineContent = { 
                     Text(
                        "Delete Playlist",
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
                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
            )
        }
    }
}
