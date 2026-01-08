package com.ngt.pixplay.ui.screens.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ngt.pixplay.data.model.Album
import com.ngt.pixplay.data.model.AudioItem
import com.ngt.pixplay.ui.components.SongListItem
import com.ngt.pixplay.ui.components.AlbumCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onSongClick: (Long) -> Unit,
    onAlbumClick: (Long) -> Unit = {},
    modifier: Modifier = Modifier,
    isPureBlack: Boolean = false,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val query by viewModel.query.collectAsState()
    val filter by viewModel.filter.collectAsState()
    val result by viewModel.searchResult.collectAsState()

    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

    // Auto-focus logic
    LaunchedEffect(Unit) {
        if (query.isEmpty()) {
            focusRequester.requestFocus()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BasicTextField(
                            value = query,
                            onValueChange = { viewModel.onQueryChange(it) },
                            modifier = Modifier
                                .weight(1f)
                                .focusRequester(focusRequester),
                            textStyle = TextStyle(
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 18.sp
                            ),
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Search
                            ),
                            keyboardActions = KeyboardActions(
                                onSearch = {
                                    focusManager.clearFocus()
                                }
                            ),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            singleLine = true,
                            decorationBox = { innerTextField ->
                                if (query.isEmpty()) {
                                    Text(
                                        text = "Search songs, albums...",
                                        style = TextStyle(
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                            fontSize = 18.sp
                                        )
                                    )
                                }
                                innerTextField()
                            }
                        )
                        
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onQueryChange("") }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear",
                                    tint = MaterialTheme.colorScheme.onSurface 
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                     Icon(
                         imageVector = Icons.Default.Search, 
                         contentDescription = null, 
                         modifier = Modifier.padding(start = 16.dp, end = 16.dp),
                         tint = MaterialTheme.colorScheme.onSurface
                     )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (isPureBlack) Color.Black else MaterialTheme.colorScheme.surfaceContainer
                )
            )
        },
        contentWindowInsets = WindowInsets.navigationBars,
        containerColor = if (isPureBlack) Color.Black else MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = filter == SearchFilter.ALL,
                    onClick = { viewModel.onFilterChange(SearchFilter.ALL) },
                    label = { Text("All") }
                )
                FilterChip(
                    selected = filter == SearchFilter.SONG,
                    onClick = { viewModel.onFilterChange(SearchFilter.SONG) },
                    label = { Text("Songs") }
                )
                FilterChip(
                    selected = filter == SearchFilter.ALBUM,
                    onClick = { viewModel.onFilterChange(SearchFilter.ALBUM) },
                    label = { Text("Albums") }
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                if (result.map.isEmpty() && query.isNotEmpty()) {
                    item {
                         Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                             Text(
                                 text = "No results found", 
                                 style = MaterialTheme.typography.bodyLarge,
                                 color = MaterialTheme.colorScheme.onSurface
                             )
                         }
                    }
                }

                
                val songs = result.map[SearchFilter.SONG]?.filterIsInstance<AudioItem>() ?: emptyList()
                val albums = result.map[SearchFilter.ALBUM]?.filterIsInstance<Album>() ?: emptyList()

                if (songs.isNotEmpty()) {
                    if (filter == SearchFilter.ALL) {
                         item {
                             Text(
                                 text = "Songs",
                                 style = MaterialTheme.typography.titleMedium,
                                 color = MaterialTheme.colorScheme.primary,
                                 modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                             )
                         }
                    }
                    items(songs) { song ->
                        SongListItem(
                            song = song,
                            onClick = { onSongClick(song.id) },
                        )
                    }
                }

                if (albums.isNotEmpty()) {
                    if (filter == SearchFilter.ALL) {
                         item {
                             Text(
                                 text = "Albums",
                                 style = MaterialTheme.typography.titleMedium,
                                 color = MaterialTheme.colorScheme.primary,
                                 modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).padding(top = 16.dp)
                             )
                         }
                    }
                    items(albums) { album ->
                         Row(
                             modifier = Modifier
                                 .fillMaxWidth()
                                 .clickable { onAlbumClick(album.id) }
                                 .padding(horizontal = 16.dp, vertical = 8.dp),
                             verticalAlignment = Alignment.CenterVertically
                         ) {
                             coil3.compose.AsyncImage(
                                 model = album.albumArtUri,
                                 contentDescription = null,
                                 modifier = Modifier.size(56.dp).padding(end = 16.dp),
                                 contentScale = androidx.compose.ui.layout.ContentScale.Crop
                             )
                             Column {
                                 Text(
                                     text = album.name, 
                                     style = MaterialTheme.typography.bodyLarge,
                                     color = MaterialTheme.colorScheme.onSurface
                                 )
                                 Text(
                                     text = album.artist, 
                                     style = MaterialTheme.typography.bodyMedium, 
                                     color = MaterialTheme.colorScheme.onSurfaceVariant
                                 )
                             }
                         }
                    }
                }
            }
        }
    }
}
