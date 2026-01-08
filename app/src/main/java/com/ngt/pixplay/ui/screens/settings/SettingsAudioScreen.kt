package com.ngt.pixplay.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsAudioScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    
    val persistentQueue by viewModel.persistentQueue.collectAsState()
    val skipSilence by viewModel.skipSilence.collectAsState()
    val audioNormalization by viewModel.audioNormalization.collectAsState()
    val audioOffload by viewModel.audioOffload.collectAsState()
    val audioFloatOutput by viewModel.audioFloatOutput.collectAsState()
    val seekInterval by viewModel.seekInterval.collectAsState()
    
    var showSeekIntervalSheet by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Audio") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                ),
                scrollBehavior = scrollBehavior
            )
        },
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            item {
                SettingsToggleItem(
                    icon = Icons.AutoMirrored.Filled.QueueMusic,
                    title = "Persistent queue",
                    subtitle = "Remember playback queue after restart",
                    checked = persistentQueue,
                    onCheckedChange = { viewModel.setPersistentQueue(it) }
                )
            }
            item {
                SettingsToggleItem(
                    icon = Icons.Default.Speed,
                    title = "Skip silence",
                    subtitle = "Skip silent parts of tracks",
                    checked = skipSilence,
                    onCheckedChange = { viewModel.setSkipSilence(it) }
                )
            }
            item {
                SettingsToggleItem(
                    icon = Icons.AutoMirrored.Filled.VolumeUp,
                    title = "Audio normalization",
                    subtitle = "Consistent volume across tracks",
                    checked = audioNormalization,
                    onCheckedChange = { viewModel.setAudioNormalization(it) }
                )
            }
            
            // Audio Offload - Disabled when 32-bit Float is enabled
            item {
                SettingsToggleItem(
                    icon = Icons.Default.BatteryChargingFull,
                    title = "Audio offload",
                    subtitle = if (audioFloatOutput) {
                        "Disabled when 32-bit Float is enabled"
                    } else {
                        "Hardware decoding for battery efficiency"
                    },
                    checked = audioOffload,
                    onCheckedChange = { 
                        viewModel.setAudioOffload(it)
                        // Disable float output when enabling offload
                        if (it) {
                            viewModel.setAudioFloatOutput(false)
                        }
                    },
                    enabled = !audioFloatOutput
                )
            }
            
            // 32-bit Float Output - Disabled when Audio Offload is enabled
            item {
                SettingsToggleItem(
                    icon = Icons.Default.HighQuality,
                    title = "32-bit Float Output",
                    subtitle = if (audioOffload) {
                        "Disabled when Audio Offload is enabled"
                    } else {
                        "High-precision audio processing (Requires restart)"
                    },
                    checked = audioFloatOutput,
                    onCheckedChange = { 
                        viewModel.setAudioFloatOutput(it)
                        // Disable offload when enabling float output
                        if (it) {
                            viewModel.setAudioOffload(false)
                        }
                    },
                    enabled = !audioOffload
                )
            }
            
            item {
                SettingsClickItem(
                    icon = Icons.Default.FastForward,
                    title = "Seek interval",
                    subtitle = "${seekInterval} seconds",
                    onClick = { showSeekIntervalSheet = true }
                )
            }
        }
    }
    
    if (showSeekIntervalSheet) {
        OptionsBottomSheet(
            title = "Seek interval",
            options = listOf(5 to "5 seconds", 10 to "10 seconds", 15 to "15 seconds", 30 to "30 seconds"),
            selectedValue = seekInterval,
            onOptionSelected = { viewModel.setSeekInterval(it); showSeekIntervalSheet = false },
            onDismiss = { showSeekIntervalSheet = false }
        )
    }
}
