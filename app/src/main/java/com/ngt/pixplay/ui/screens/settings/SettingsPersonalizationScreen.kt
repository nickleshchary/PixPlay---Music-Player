package com.ngt.pixplay.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsPersonalizationScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    
    // Collect settings state
    val darkMode by viewModel.darkMode.collectAsState()
    val pureBlack by viewModel.pureBlack.collectAsState()
    val dynamicColors by viewModel.dynamicColors.collectAsState()
    val gridItemSize by viewModel.gridItemSize.collectAsState()
    
    // Bottom sheet states
    var showDarkModeSheet by remember { mutableStateOf(false) }
    var showGridSizeSheet by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Personalization") },
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
                    icon = Icons.Default.Bedtime,
                    title = "Dark mode",
                    subtitle = when(darkMode) { "light" -> "Light"; "dark" -> "Dark"; else -> "Follow system" },
                    onClick = { showDarkModeSheet = true }
                )
            }
            item {
                SettingsToggleItem(
                    icon = Icons.Default.Contrast,
                    title = "Pure black",
                    subtitle = if (darkMode == "dark") "Use pure black for OLED screens" else "Enable dark mode first",
                    checked = pureBlack,
                    onCheckedChange = { viewModel.setPureBlack(it) },
                    enabled = darkMode == "dark"
                )
            }
            item {
                SettingsToggleItem(
                    icon = Icons.Default.Palette,
                    title = "Album art colors",
                    subtitle = "Adapt color scheme to current song",
                    checked = dynamicColors,
                    onCheckedChange = { viewModel.setDynamicColors(it) }
                )
            }
            item {
                SettingsClickItem(
                    icon = Icons.Default.GridView,
                    title = "Grid item size",
                    subtitle = gridItemSize.replaceFirstChar { it.uppercase() },
                    onClick = { showGridSizeSheet = true }
                )
            }
        }
    }
    
    if (showDarkModeSheet) {
        OptionsBottomSheet(
            title = "Dark mode",
            options = listOf("system" to "Follow system", "light" to "Light", "dark" to "Dark"),
            selectedValue = darkMode,
            onOptionSelected = { viewModel.setDarkMode(it); showDarkModeSheet = false },
            onDismiss = { showDarkModeSheet = false }
        )
    }
    
    if (showGridSizeSheet) {
        OptionsBottomSheet(
            title = "Grid item size",
            options = listOf("small" to "Small", "medium" to "Medium", "large" to "Large"),
            selectedValue = gridItemSize,
            onOptionSelected = { viewModel.setGridItemSize(it); showGridSizeSheet = false },
            onDismiss = { showGridSizeSheet = false }
        )
    }
}
