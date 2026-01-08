package com.ngt.pixplay.ui.screens.library

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ngt.pixplay.ui.components.LibraryCategoryGridItem
import com.ngt.pixplay.ui.components.LibraryCategoryListItem
import com.ngt.pixplay.ui.components.ViewToggle
import com.ngt.pixplay.ui.components.LibraryViewMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    onNavigateToSongs: () -> Unit,
    onNavigateToAlbums: () -> Unit,
    onNavigateToArtists: () -> Unit,
    onNavigateToFavorites: () -> Unit,
    onNavigateToPlaylists: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val isGrid by viewModel.isGrid.collectAsState()
    
    // Counts
    val songs by viewModel.songs.collectAsState()
    val albums by viewModel.albums.collectAsState()
    val artists by viewModel.artists.collectAsState()
    val playlists by viewModel.playlists.collectAsState()
    val favoriteSongs by viewModel.favoriteSongs.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Library") },
                actions = {
                    ViewToggle(
                        viewMode = if (isGrid) LibraryViewMode.GRID else LibraryViewMode.LIST,
                        onToggle = { viewModel.toggleView() }
                    )
                },
                scrollBehavior = scrollBehavior
            )
        },
        contentWindowInsets = WindowInsets.navigationBars,
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { paddingValues ->
        val bottomPadding = com.ngt.pixplay.ui.common.LocalContentPadding.current.calculateBottomPadding()

        if (isGrid) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(160.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = paddingValues.calculateTopPadding()),

                contentPadding = PaddingValues(16.dp).run { 
                    PaddingValues(
                        start = calculateLeftPadding(androidx.compose.ui.platform.LocalLayoutDirection.current),
                        top = calculateTopPadding(),
                        end = calculateRightPadding(androidx.compose.ui.platform.LocalLayoutDirection.current),
                        bottom = calculateBottomPadding() + bottomPadding
                    )
                },
            ) {
                item {
                    LibraryCategoryGridItem(
                        title = "Liked Songs (${favoriteSongs.size})",
                        subtitle = "",
                        icon = Icons.Default.Favorite,
                        onClick = onNavigateToFavorites
                    )
                }
                item {
                    LibraryCategoryGridItem(
                        title = "All Songs (${songs.size})",
                        subtitle = "",
                        icon = Icons.Default.MusicNote,
                        onClick = onNavigateToSongs
                    )
                }
                item {
                    LibraryCategoryGridItem(
                        title = "Albums (${albums.size})",
                        subtitle = "",
                        icon = Icons.Default.Album,
                        onClick = onNavigateToAlbums
                    )
                }
                item {
                    LibraryCategoryGridItem(
                        title = "Artists (${artists.size})",
                        subtitle = "",
                        icon = Icons.Default.Person,
                        onClick = onNavigateToArtists
                    )
                }
                item {
                    LibraryCategoryGridItem(
                        title = "Playlists (${playlists.size})",
                        subtitle = "",
                        icon = Icons.AutoMirrored.Filled.QueueMusic,
                        onClick = onNavigateToPlaylists
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = paddingValues.calculateTopPadding()),

                contentPadding = PaddingValues(bottom = bottomPadding)
            ) {
                item {
                    LibraryCategoryListItem(
                        title = "Liked Songs",
                        subtitle = "${favoriteSongs.size} songs",
                        icon = Icons.Default.Favorite,
                        onClick = onNavigateToFavorites
                    )
                }
                item {
                    LibraryCategoryListItem(
                        title = "All Songs",
                        subtitle = "${songs.size} songs",
                        icon = Icons.Default.MusicNote,
                        onClick = onNavigateToSongs
                    )
                }
                item {
                    LibraryCategoryListItem(
                        title = "Albums",
                        subtitle = "${albums.size} albums",
                        icon = Icons.Default.Album,
                        onClick = onNavigateToAlbums
                    )
                }
                item {
                    LibraryCategoryListItem(
                        title = "Artists",
                        subtitle = "${artists.size} artists",
                        icon = Icons.Default.Person,
                        onClick = onNavigateToArtists
                    )
                }
                item {
                    LibraryCategoryListItem(
                        title = "Playlists",
                        subtitle = "${playlists.size} playlists",
                        icon = Icons.AutoMirrored.Filled.QueueMusic,
                        onClick = onNavigateToPlaylists
                    )
                }
            }
        }
    }
}
