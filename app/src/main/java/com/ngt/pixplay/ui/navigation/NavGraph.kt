package com.ngt.pixplay.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.ngt.pixplay.ui.screens.album.AlbumScreen
import com.ngt.pixplay.ui.screens.discover.DiscoverScreen
import com.ngt.pixplay.ui.screens.library.LibraryScreen
import com.ngt.pixplay.ui.screens.settings.SettingsScreen

sealed class Screen(val route: String) {
    data object Discover : Screen("discover")
    data object Library : Screen("library")
    
    // Library sub-screens
    data object LibrarySongs : Screen("library/songs")
    data object LibraryAlbums : Screen("library/albums")
    data object LibraryArtists : Screen("library/artists")
    data object LibraryFavorites : Screen("library/favorites")
    data object LibraryPlaylists : Screen("library/playlists")
    data object Album : Screen("album/{albumId}") {
        fun createRoute(albumId: Long) = "album/$albumId"
    }
    data object Playlist : Screen("playlist/{playlistId}") {
        fun createRoute(playlistId: Long) = "playlist/$playlistId"
    }
    data object Artist : Screen("artist/{artistId}") {
        fun createRoute(artistId: Long) = "artist/$artistId"
    }
    data object Settings : Screen("settings")
    data object SettingsPersonalization : Screen("settings/personalization")
    data object SettingsPlayerUI : Screen("settings/player_ui")
    data object SettingsAudio : Screen("settings/audio")
    data object SettingsAbout : Screen("settings/about")
    data object Search : Screen("search")
    data object Permission : Screen("permission")
}

@Composable
fun NavGraph(
    navController: NavHostController,
    onPlaySong: (Long) -> Unit,
    onShuffleAll: () -> Unit,
    startDestination: String = Screen.Discover.route,
    isDarkTheme: Boolean = false,
    isPureBlack: Boolean = false,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Screen.Permission.route) {
            com.ngt.pixplay.ui.screens.welcome.PermissionScreen(
                onPermissionsGranted = {
                    navController.navigate(Screen.Discover.route) {
                        popUpTo(Screen.Permission.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Discover.route) {
            DiscoverScreen(
                onSongClick = onPlaySong,
                onAlbumClick = { albumId ->
                    navController.navigate(Screen.Album.createRoute(albumId))
                },
                onSeeAllAlbumsClick = {
                    navController.navigate(Screen.LibraryAlbums.route)
                },
                onSeeAllSongsClick = {
                    navController.navigate(Screen.LibrarySongs.route)
                },
                onSettingsClick = {
                    navController.navigate(Screen.Settings.route)
                },
                onShuffleClick = onShuffleAll
            )
        }

        composable(Screen.Search.route) {
            com.ngt.pixplay.ui.screens.search.SearchScreen(
                onSongClick = onPlaySong,
                onAlbumClick = { albumId ->
                    navController.navigate(Screen.Album.createRoute(albumId))
                },
                isPureBlack = isPureBlack
            )
        }
        
        composable(Screen.Library.route) {
            LibraryScreen(
                onNavigateToSongs = { navController.navigate(Screen.LibrarySongs.route) },
                onNavigateToAlbums = { navController.navigate(Screen.LibraryAlbums.route) },
                onNavigateToArtists = { navController.navigate(Screen.LibraryArtists.route) },
                onNavigateToFavorites = { navController.navigate(Screen.LibraryFavorites.route) },
                onNavigateToPlaylists = { navController.navigate(Screen.LibraryPlaylists.route) }
            )
        }

        composable(Screen.LibrarySongs.route) {
            com.ngt.pixplay.ui.screens.library.LibrarySongsScreen(
                onSongClick = onPlaySong,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.LibraryAlbums.route) {
            com.ngt.pixplay.ui.screens.library.LibraryAlbumsScreen(
                onAlbumClick = { albumId ->
                    navController.navigate(Screen.Album.createRoute(albumId))
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.LibraryArtists.route) {
            com.ngt.pixplay.ui.screens.library.LibraryArtistsScreen(
                onArtistClick = { artistId ->
                    navController.navigate(Screen.Artist.createRoute(artistId))
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Artist.route,
            arguments = listOf(navArgument("artistId") { type = NavType.LongType })
        ) {
            com.ngt.pixplay.ui.screens.artist.ArtistDetailScreen(
                onBackClick = { navController.popBackStack() },
                onAlbumClick = { albumId -> 
                     navController.navigate(Screen.Album.createRoute(albumId))
                },
                onSongClick = onPlaySong
            )
        }

        composable(Screen.LibraryFavorites.route) {
            com.ngt.pixplay.ui.screens.library.LibraryFavoritesScreen(
                onSongClick = onPlaySong,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.LibraryPlaylists.route) {
            com.ngt.pixplay.ui.screens.library.LibraryPlaylistsScreen(
                onPlaylistClick = { playlistId ->
                    navController.navigate(Screen.Playlist.createRoute(playlistId))
                },
                onBackClick = { navController.popBackStack() }
            )
        }
        
        composable(
            route = Screen.Album.route,
            arguments = listOf(navArgument("albumId") { type = NavType.LongType })
        ) {
            AlbumScreen(
                onBackClick = { navController.popBackStack() },
                onSongClick = onPlaySong,
                isDarkTheme = isDarkTheme,
                isPureBlack = isPureBlack
            )
        }

        composable(
            route = Screen.Playlist.route,
            arguments = listOf(navArgument("playlistId") { type = NavType.LongType })
        ) { backStackEntry ->
            val playlistId = backStackEntry.arguments?.getLong("playlistId") ?: return@composable
            com.ngt.pixplay.ui.screens.playlist.PlaylistDetailScreen(
                playlistId = playlistId,
                onBackClick = { navController.popBackStack() },
                onSongClick = onPlaySong
            )
        }
        
        composable(Screen.Settings.route) {
            SettingsScreen(
                onBackClick = { navController.popBackStack() },
                onNavigateTo = { route -> navController.navigate(route) }
            )
        }

        composable(Screen.SettingsPersonalization.route) {
            com.ngt.pixplay.ui.screens.settings.SettingsPersonalizationScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.SettingsPlayerUI.route) {
            com.ngt.pixplay.ui.screens.settings.SettingsPlayerUIScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.SettingsAudio.route) {
            com.ngt.pixplay.ui.screens.settings.SettingsAudioScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.SettingsAbout.route) {
            com.ngt.pixplay.ui.screens.settings.SettingsAboutScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
