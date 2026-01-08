package com.ngt.pixplay.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.ngt.pixplay.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onNavigateTo: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
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
                SettingsClickItem(
                    icon = Icons.Default.Palette,
                    title = "Personalization",
                    subtitle = "Theme, colors, grid options",
                    onClick = { onNavigateTo(Screen.SettingsPersonalization.route) }
                )
            }
            item {
                SettingsClickItem(
                    icon = Icons.Default.Tune,
                    title = "Player UI",
                    subtitle = "Background, slider, lyrics, gestures",
                    onClick = { onNavigateTo(Screen.SettingsPlayerUI.route) }
                )
            }
            item {
                SettingsClickItem(
                    icon = Icons.AutoMirrored.Filled.VolumeUp,
                    title = "Audio",
                    subtitle = "Quality, playback options, normalization",
                    onClick = { onNavigateTo(Screen.SettingsAudio.route) }
                )
            }
            item {
                SettingsClickItem(
                    icon = Icons.Default.Info,
                    title = "About",
                    subtitle = "App version and info",
                    onClick = { onNavigateTo(Screen.SettingsAbout.route) }
                )
            }
        }
    }
}
