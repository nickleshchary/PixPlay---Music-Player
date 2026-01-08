package com.ngt.pixplay.ui.components

import android.content.Intent
import android.media.audiofx.AudioEffect
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.ngt.pixplay.data.model.AudioItem
import kotlin.math.log2
import kotlin.math.pow
import kotlin.math.round

/**
 * Player Menu Bottom Sheet - Shows advanced player options like volume, equalizer, pitch/speed
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerMenuBottomSheet(
    currentSong: AudioItem,
    isVisible: Boolean,
    onDismiss: () -> Unit,
    currentVolume: Float = 1f,
    onVolumeChange: (Float) -> Unit = {},
    currentSpeed: Float = 1f,
    onSpeedChange: (Float) -> Unit = {},
    currentPitch: Float = 1f,
    onPitchChange: (Float) -> Unit = {},
    audioSessionId: Int = 0,
    onShowQueue: () -> Unit = {},
    onAddToPlaylist: () -> Unit = {},
    onAlbumClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    if (isVisible) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            contentColor = MaterialTheme.colorScheme.onSurface,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            PlayerMenuContent(
                currentSong = currentSong,
                currentVolume = currentVolume,
                onVolumeChange = onVolumeChange,
                currentSpeed = currentSpeed,
                onSpeedChange = onSpeedChange,
                currentPitch = currentPitch,
                onPitchChange = onPitchChange,
                audioSessionId = audioSessionId,
                onShowQueue = onShowQueue,
                onAddToPlaylist = onAddToPlaylist,
                onAlbumClick = onAlbumClick,
                onDismiss = onDismiss
            )

        }
    }
}

@Composable
private fun PlayerMenuContent(
    currentSong: AudioItem,
    currentVolume: Float,
    onVolumeChange: (Float) -> Unit,
    currentSpeed: Float,
    onSpeedChange: (Float) -> Unit,
    currentPitch: Float,
    onPitchChange: (Float) -> Unit,
    audioSessionId: Int,
    onShowQueue: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onAlbumClick: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var showDetailsDialog by rememberSaveable { mutableStateOf(false) }
    var showPitchTempoDialog by rememberSaveable { mutableStateOf(false) }
    
    val activityResultLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 32.dp)
    ) {
        // Header with song info
        ListItem(
            headlineContent = { 
                Text(
                    text = currentSong.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                ) 
            },
            supportingContent = { 
                Text(
                    text = currentSong.artist,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                ) 
            },
            leadingContent = {
                 AsyncImage(
                    model = currentSong.albumArtUri,
                    contentDescription = null,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            },
            colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
        )

        // Volume Control
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp, bottom = 8.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = "Volume",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Slider(
                    value = currentVolume,
                    onValueChange = onVolumeChange,
                    modifier = Modifier.weight(1f),
                    valueRange = 0f..1f
                )
                
                Text(
                    text = "${(currentVolume * 100).toInt()}%",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.width(48.dp),
                    textAlign = TextAlign.End
                )
            }
        }

        // Show Queue
        ListItem(
            headlineContent = { Text("Show queue") },
            supportingContent = { Text("View upcoming songs") },
            leadingContent = { 
                Icon(
                    Icons.AutoMirrored.Filled.QueueMusic, 
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                ) 
            },
            modifier = Modifier.clickable {
                onShowQueue()
                onDismiss()
            },
            colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
        )
        
        // Add to Playlist
        ListItem(
            headlineContent = { Text("Add to playlist") },
            supportingContent = { Text("Save to playlist") },
            leadingContent = { 
                Icon(
                    Icons.AutoMirrored.Filled.PlaylistAdd, 
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                ) 
            },
            modifier = Modifier.clickable {
                onAddToPlaylist()
                onDismiss()
            },
            colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
        )
        
        // Go to Album
        ListItem(
            headlineContent = { Text("Go to album") },
            supportingContent = { 
                Text(
                    text = currentSong.album,
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
                onAlbumClick()
                onDismiss()
            },
            colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
        )
        
        // Equalizer
        ListItem(
            headlineContent = { Text("Equalizer") },
            supportingContent = { Text("Open system audio equalizer") },
            leadingContent = { 
                Icon(
                    Icons.Default.Equalizer, 
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                ) 
            },
            modifier = Modifier.clickable {
                val intent = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL).apply {
                    putExtra(AudioEffect.EXTRA_AUDIO_SESSION, audioSessionId)
                    putExtra(AudioEffect.EXTRA_PACKAGE_NAME, context.packageName)
                    putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC)
                }
                if (intent.resolveActivity(context.packageManager) != null) {
                    activityResultLauncher.launch(intent)
                } else {
                    Toast.makeText(context, "No equalizer app found", Toast.LENGTH_SHORT).show()
                }
                onDismiss()
            },
            colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
        )
        
        // Pitch & Speed (Advanced)
        ListItem(
            headlineContent = { Text("Pitch & Speed") },
            supportingContent = { Text("Adjust playback speed and pitch") },
            leadingContent = { 
                Icon(
                    Icons.Default.Speed,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                ) 
            },
            modifier = Modifier.clickable { showPitchTempoDialog = true },
            colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
        )
        
        // Song Details
        ListItem(
            headlineContent = { Text("Song details") },
            supportingContent = { Text("View detailed information") },
            leadingContent = { 
                Icon(
                    Icons.Default.Info, 
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                ) 
            },
            modifier = Modifier.clickable { showDetailsDialog = true },
            colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
        )
    }
    
    // Pitch & Speed Dialog
    if (showPitchTempoDialog) {
        PitchSpeedDialog(
            currentSpeed = currentSpeed,
            currentPitch = currentPitch,
            onSpeedChange = onSpeedChange,
            onPitchChange = onPitchChange,
            onDismiss = { showPitchTempoDialog = false }
        )
    }
    
    // Song Details Dialog
    if (showDetailsDialog) {
        SongDetailsDialog(
            song = currentSong,
            onDismiss = { showDetailsDialog = false }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PitchSpeedDialog(
    currentSpeed: Float,
    currentPitch: Float,
    onSpeedChange: (Float) -> Unit,
    onPitchChange: (Float) -> Unit,
    onDismiss: () -> Unit
) {
    var speed by remember { mutableFloatStateOf(currentSpeed) }
    var transposeValue by remember { mutableFloatStateOf(round(12 * log2(currentPitch))) }
    
    val speedPresets = listOf(0.5f, 0.8f, 1.0f, 1.25f, 1.5f, 2.0f, 3.0f)

    AlertDialog(
        properties = DialogProperties(usePlatformDefaultWidth = false),
        onDismissRequest = onDismiss,
        title = { Text("Playback Control") },
        dismissButton = {
            TextButton(
                onClick = {
                    speed = 1f
                    transposeValue = 0f
                    onSpeedChange(1f)
                    onPitchChange(1f)
                }
            ) {
                Text("Reset")
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onDismiss()
            }) {
                Text("Done")
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Speed Control
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = "Speed",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "x${String.format("%.2f", speed)}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Slider(
                        value = speed,
                        onValueChange = { 
                            speed = it
                            onSpeedChange(it) 
                        },
                        valueRange = 0.25f..3.0f,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    // Speed Presets
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        speedPresets.forEach { preset ->
                            FilterChip(
                                selected = kotlin.math.abs(speed - preset) < 0.05f,
                                onClick = { 
                                    speed = preset
                                    onSpeedChange(preset)
                                },
                                label = { Text("${preset}x") }
                            )
                        }
                    }
                }
                
                HorizontalDivider()
                
                // Pitch Control
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = "Pitch",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "${if (transposeValue > 0) "+" else ""}${transposeValue.toInt()}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                     Text(
                        text = "Semitones",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Slider(
                        value = transposeValue,
                        onValueChange = { 
                            transposeValue = round(it)
                            val newPitch = 2f.pow(transposeValue / 12)
                            onPitchChange(newPitch)
                        },
                        valueRange = -12f..12f,
                        steps = 23,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .padding(16.dp)
    )
}
