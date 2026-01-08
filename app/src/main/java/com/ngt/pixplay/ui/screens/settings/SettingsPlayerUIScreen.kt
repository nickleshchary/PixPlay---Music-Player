package com.ngt.pixplay.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.FormatAlignLeft
import androidx.compose.material.icons.filled.Animation
import androidx.compose.material.icons.filled.Flare
import androidx.compose.material.icons.filled.FormatLineSpacing
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Gradient
import androidx.compose.material.icons.filled.Swipe
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsPlayerUIScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    
    val playerBackground by viewModel.playerBackground.collectAsState()
    val sliderStyle by viewModel.sliderStyle.collectAsState()
    val swipeGestures by viewModel.swipeGestures.collectAsState()
    
    // Lyrics settings state
    val lyricsTextPosition by viewModel.lyricsTextPosition.collectAsState()
    val lyricsAnimationStyle by viewModel.lyricsAnimationStyle.collectAsState()
    val lyricsGlowEffect by viewModel.lyricsGlowEffect.collectAsState()
    val lyricsTextSize by viewModel.lyricsTextSize.collectAsState()
    val lyricsLineSpacing by viewModel.lyricsLineSpacing.collectAsState()
    
    // Bottom sheet states
    var showBackgroundSheet by remember { mutableStateOf(false) }
    var showSliderStyleSheet by remember { mutableStateOf(false) }
    
    var showLyricsPositionSheet by remember { mutableStateOf(false) }
    var showLyricsAnimationSheet by remember { mutableStateOf(false) }
    var showLyricsTextSizeSheet by remember { mutableStateOf(false) }
    var showLyricsLineSpacingSheet by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Player UI") },
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
            item { SettingsCategory("General") }
            
            item {
                SettingsClickItem(
                    icon = Icons.Default.Gradient,
                    title = "Player background",
                    subtitle = playerBackground.replaceFirstChar { it.uppercase() },
                    onClick = { showBackgroundSheet = true }
                )
            }
            item {
                SettingsClickItem(
                    icon = Icons.Default.Tune,
                    title = "Slider style",
                    subtitle = sliderStyle.replaceFirstChar { it.uppercase() },
                    onClick = { showSliderStyleSheet = true }
                )
            }
            item {
                SettingsToggleItem(
                    icon = Icons.Default.Swipe,
                    title = "Swipe gestures",
                    subtitle = "Swipe to change tracks",
                    checked = swipeGestures,
                    onCheckedChange = { viewModel.setSwipeGestures(it) }
                )
            }
            
            item { SettingsCategory("Lyrics") }
            
            item {
                SettingsClickItem(
                    icon = Icons.AutoMirrored.Filled.FormatAlignLeft, 
                    title = "Text position",
                    subtitle = lyricsTextPosition.lowercase().replaceFirstChar { it.uppercase() },
                    onClick = { showLyricsPositionSheet = true }
                )
            }
            item {
                SettingsClickItem(
                    icon = Icons.Default.Animation, 
                    title = "Animation style",
                    subtitle = lyricsAnimationStyle.lowercase().replaceFirstChar { it.uppercase() },
                    onClick = { showLyricsAnimationSheet = true }
                )
            }
            item {
                SettingsToggleItem(
                    icon = Icons.Default.Flare, 
                    title = "Glow effect",
                    subtitle = "Add glow to active line",
                    checked = lyricsGlowEffect,
                    onCheckedChange = { viewModel.setLyricsGlowEffect(it) }
                )
            }
            item {
                SettingsClickItem(
                    icon = Icons.Default.FormatSize, 
                    title = "Text size",
                    subtitle = "${lyricsTextSize.toInt()} sp",
                    onClick = { showLyricsTextSizeSheet = true }
                )
            }
            item {
                SettingsClickItem(
                    icon = Icons.Default.FormatLineSpacing, 
                    title = "Line spacing",
                    subtitle = "${lyricsLineSpacing}x",
                    onClick = { showLyricsLineSpacingSheet = true }
                )
            }
        }
    }
    
    if (showBackgroundSheet) {
        OptionsBottomSheet(
            title = "Player background",
            options = listOf("blur" to "Blur", "gradient" to "Gradient", "solid" to "Solid color"),
            selectedValue = playerBackground,
            onOptionSelected = { viewModel.setPlayerBackground(it); showBackgroundSheet = false },
            onDismiss = { showBackgroundSheet = false }
        )
    }
    
    if (showSliderStyleSheet) {
        OptionsBottomSheet(
            title = "Slider style",
            options = listOf("default" to "Default", "squiggly" to "Squiggly", "slim" to "Slim"),
            selectedValue = sliderStyle,
            onOptionSelected = { viewModel.setSliderStyle(it); showSliderStyleSheet = false },
            onDismiss = { showSliderStyleSheet = false }
        )
    }
    
    if (showLyricsPositionSheet) {
        OptionsBottomSheet(
            title = "Text position",
            options = listOf("LEFT" to "Left", "CENTER" to "Center", "RIGHT" to "Right"),
            selectedValue = lyricsTextPosition,
            onOptionSelected = { viewModel.setLyricsTextPosition(it); showLyricsPositionSheet = false },
            onDismiss = { showLyricsPositionSheet = false }
        )
    }
    
    if (showLyricsAnimationSheet) {
        OptionsBottomSheet(
            title = "Animation style",
            options = listOf(
                "NONE" to "None", 
                "FADE" to "Fade", 
                "GLOW" to "Glow", 
                "SLIDE" to "Slide", 
                "KARAOKE" to "Karaoke", 
                "APPLE" to "Apple Music"
            ),
            selectedValue = lyricsAnimationStyle,
            onOptionSelected = { viewModel.setLyricsAnimationStyle(it); showLyricsAnimationSheet = false },
            onDismiss = { showLyricsAnimationSheet = false }
        )
    }
    
    if (showLyricsTextSizeSheet) {
        OptionsBottomSheet(
            title = "Text size",
            options = listOf(
                16f to "Small (16sp)", 
                20f to "Medium (20sp)", 
                24f to "Default (24sp)", 
                28f to "Large (28sp)", 
                32f to "Extra Large (32sp)"
            ),
            selectedValue = lyricsTextSize,
            onOptionSelected = { viewModel.setLyricsTextSize(it); showLyricsTextSizeSheet = false },
            onDismiss = { showLyricsTextSizeSheet = false }
        )
    }
    
    if (showLyricsLineSpacingSheet) {
        OptionsBottomSheet(
            title = "Line spacing",
            options = listOf(
                1.0f to "Compact (1.0x)", 
                1.3f to "Default (1.3x)", 
                1.6f to "Relaxed (1.6x)", 
                2.0f to "Double (2.0x)"
            ),
            selectedValue = lyricsLineSpacing,
            onOptionSelected = { viewModel.setLyricsLineSpacing(it); showLyricsLineSpacingSheet = false },
            onDismiss = { showLyricsLineSpacingSheet = false }
        )
    }
}
