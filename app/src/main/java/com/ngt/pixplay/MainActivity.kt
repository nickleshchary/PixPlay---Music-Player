package com.ngt.pixplay

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ngt.pixplay.data.preferences.SettingsDataStore
import com.ngt.pixplay.service.MediaPlaybackService
import com.ngt.pixplay.ui.navigation.NavGraph
import com.ngt.pixplay.ui.navigation.Screen
import com.ngt.pixplay.ui.player.BottomSheet
import com.ngt.pixplay.ui.player.ExpandedPlayerContent
import com.ngt.pixplay.ui.player.MiniPlayerContent
import com.ngt.pixplay.ui.player.MiniPlayerHeight
import com.ngt.pixplay.ui.player.rememberBottomSheetState
import com.ngt.pixplay.ui.screens.nowplaying.NowPlayingViewModel
import com.ngt.pixplay.ui.screens.settings.SettingsViewModel // Import SettingsViewModel
import com.ngt.pixplay.ui.theme.PixPlayTheme
import com.ngt.pixplay.ui.components.SliderStyle
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.mutableStateOf
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.toBitmap
import com.ngt.pixplay.ui.theme.PlayerColorExtractor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

val NavigationBarHeight = 80.dp

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var settingsDataStore: SettingsDataStore
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Check if permissions granted
    }
    
    // Service connection for MediaPlaybackService
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            // Service is now connected - media controls will be available
        }
        
        override fun onServiceDisconnected(name: ComponentName?) {
            // Service disconnected
        }
    }
    
    override fun onStart() {
        super.onStart()
        // Start and bind the MediaPlaybackService for system media controls
        val intent = Intent(this, MediaPlaybackService::class.java)
        // Use regular startService - MediaSessionService will auto-promote to foreground
        // when playback starts (no manual notification needed)
        startService(intent)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }
    
    override fun onStop() {
        unbindService(serviceConnection)
        super.onStop()
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Request audio permission
        // Permissions handled by PermissionScreen if needed
        // requestAudioPermission() removed
        
        setContent {
            // Read theme settings
            val darkModeSetting by settingsDataStore.darkMode.collectAsState(initial = "system")
            val pureBlack by settingsDataStore.pureBlack.collectAsState(initial = false)
            val dynamicColors by settingsDataStore.dynamicColors.collectAsState(initial = true)
            
            // Determine actual dark theme
            val systemDarkTheme = isSystemInDarkTheme()
            val darkTheme = when (darkModeSetting) {
                "light" -> false
                "dark" -> true
                else -> systemDarkTheme
            }
            
            
            var themeColor by remember { mutableStateOf<Color?>(null) }
            val nowPlayingViewModel: NowPlayingViewModel = hiltViewModel()
            val playbackState by nowPlayingViewModel.playbackState.collectAsState()
            
            LaunchedEffect(playbackState.currentSong) {
                val song = playbackState.currentSong
                if (song != null) {
                    val artUri = song.albumArtUri
                    if (artUri != null) {
                       withContext(Dispatchers.IO) {
                           val request = ImageRequest.Builder(this@MainActivity)
                               .data(artUri)
                               .allowHardware(false)
                               .build()
                           val result = coil3.SingletonImageLoader.get(this@MainActivity).execute(request)
                           if (result is coil3.request.SuccessResult) {
                               themeColor = PlayerColorExtractor.extractSeedColor(result.image.toBitmap(), Color.Black)
                           }
                       }
                    } else {
                        themeColor = null
                    }
                } else {
                    themeColor = null
                }
            }

            PixPlayTheme(
                darkTheme = darkTheme,
                dynamicColor = true, // Always enable wallpaper colors as base (fallback)
                pureBlack = pureBlack,
                seedColor = if (dynamicColors) themeColor else null // Only use album art if enabled
            ) {
                // Read slider style
                val sliderStyleSetting by settingsDataStore.sliderStyle.collectAsState(initial = "default")
                val sliderStyle = when (sliderStyleSetting) {
                    "squiggly" -> SliderStyle.SQUIGGLY
                    "slim" -> SliderStyle.SLIM
                    else -> SliderStyle.DEFAULT
                }
                
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(
                        sliderStyle = sliderStyle,
                        isDarkTheme = darkTheme,
                        isPureBlack = pureBlack,
                        themeColor = if (dynamicColors) themeColor else null
                    )
                }
            }
        }
    }
    
    private fun requestAudioPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissions = arrayOf(
                Manifest.permission.READ_MEDIA_AUDIO,
                Manifest.permission.POST_NOTIFICATIONS
            )
            requestPermissionLauncher.launch(permissions)
        } else {
            val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            requestPermissionLauncher.launch(permissions)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    sliderStyle: SliderStyle = SliderStyle.DEFAULT,
    isDarkTheme: Boolean = false,
    isPureBlack: Boolean = false,
    themeColor: Color? = null
) {
    val navController = rememberNavController()
    val context = androidx.compose.ui.platform.LocalContext.current
    var selectedItem by rememberSaveable { mutableIntStateOf(0) }
    
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val density = LocalDensity.current
    
    // Detect detail screens that should hide the navigation bar
    val isDetailScreen = remember(currentRoute) {
        currentRoute?.startsWith("album/") == true ||
        currentRoute?.startsWith("artist/") == true ||
        currentRoute?.startsWith("playlist/") == true ||
        (currentRoute?.startsWith("library/") == true && currentRoute != "library") ||
        currentRoute?.startsWith("settings") == true ||
        currentRoute == Screen.Permission.route
    }
    
    // Update selected item based on current route
    selectedItem = when {
        currentRoute == Screen.Discover.route -> 0
        currentRoute == Screen.Search.route -> 1
        currentRoute?.startsWith("library") == true -> 2
        else -> selectedItem
    }
    
    // Get playback state from ViewModel
    val nowPlayingViewModel: NowPlayingViewModel = hiltViewModel()
    // Playlist ViewModel
    val playlistViewModel: com.ngt.pixplay.ui.screens.playlist.PlaylistViewModel = hiltViewModel()
    val playlists by playlistViewModel.playlists.collectAsState()
    var showAddToPlaylistSheet by remember { mutableStateOf(false) }
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }

    val playbackState by nowPlayingViewModel.playbackState.collectAsState()
    
    // Get settings from SettingsViewModel
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val showLyrics by settingsViewModel.showLyrics.collectAsState()
    val lyricsTextPosition by settingsViewModel.lyricsTextPosition.collectAsState()
    val lyricsAnimationStyle by settingsViewModel.lyricsAnimationStyle.collectAsState()
    val lyricsGlowEffect by settingsViewModel.lyricsGlowEffect.collectAsState()
    val lyricsTextSize by settingsViewModel.lyricsTextSize.collectAsState()
    val lyricsLineSpacing by settingsViewModel.lyricsLineSpacing.collectAsState()
    val seekInterval by settingsViewModel.seekInterval.collectAsState()
    val swipeGestures by settingsViewModel.swipeGestures.collectAsState()
    val playerBackground by settingsViewModel.playerBackground.collectAsState()
    
    val hasCurrentSong = playbackState.currentSong != null
    
    // Get system insets
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val maxHeight = maxHeight
        
        // Spacing between mini player and nav bar
        val miniPlayerSpacing = 8.dp
        
        // Collapsed bound: different for detail screens (no nav bar) vs main screens
        val collapsedBound = if (isDetailScreen) {
            // On detail screens, mini player sits just above system insets
            bottomInset + miniPlayerSpacing + MiniPlayerHeight
        } else {
            // On main screens, mini player sits above nav bar
            bottomInset + NavigationBarHeight + miniPlayerSpacing + MiniPlayerHeight
        }
        
        // Bottom sheet state
        val playerSheetState = rememberBottomSheetState(
            dismissedBound = 0.dp,
            expandedBound = maxHeight,
            collapsedBound = collapsedBound,
            initialAnchor = 0 // Start dismissed
        )
        
        // When a song is selected, collapse the sheet to show mini player
        LaunchedEffect(hasCurrentSong) {
            if (hasCurrentSong && playerSheetState.isDismissed) {
                playerSheetState.collapseSoft()
            } else if (!hasCurrentSong && !playerSheetState.isDismissed) {
                playerSheetState.dismiss()
            }
        }
        
        // Calculate content padding for main content
        val contentBottomPadding = remember(hasCurrentSong, playerSheetState.isExpanded, isDetailScreen) {
            if (playerSheetState.isExpanded) {
                0.dp
            } else {
                val basePadding = if (isDetailScreen) 0.dp else NavigationBarHeight
                if (hasCurrentSong) {
                    basePadding + MiniPlayerHeight + miniPlayerSpacing + 32.dp
                } else {
                    basePadding + 16.dp
                }
            }
        }
        
        // Check permissions
        val hasPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
        
        val startDestination = if (hasPermissions) Screen.Discover.route else Screen.Permission.route
 
        // Provide content padding to screens
        val contentPadding = PaddingValues(bottom = contentBottomPadding)
        
        com.ngt.pixplay.ui.common.LocalContentPadding provides contentPadding
        
        // Main content area
        androidx.compose.runtime.CompositionLocalProvider(
            com.ngt.pixplay.ui.common.LocalContentPadding provides contentPadding
        ) {
            NavGraph(
                navController = navController,
                startDestination = startDestination,
                onPlaySong = { id -> nowPlayingViewModel.playSong(id) },
                onShuffleAll = { nowPlayingViewModel.shuffleAll() },
                isDarkTheme = isDarkTheme,
                isPureBlack = isPureBlack,
                modifier = Modifier.fillMaxSize()
            )
        }
        
        // Bottom section: Navigation bar + Player
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxSize()
        ) {
            // Bottom Sheet Player - positioned above nav bar when collapsed
            if (hasCurrentSong) {
                val currentSong = playbackState.currentSong!!
 
                // Calculate player-specific color scheme (Always use Album Art if available, like ExpandedPlayer)
                val playerColorScheme = if (themeColor != null) {
                     com.materialkolor.rememberDynamicColorScheme(
                        seedColor = themeColor!!,
                        isDark = isDarkTheme,
                        isAmoled = isPureBlack
                    )
                } else {
                    MaterialTheme.colorScheme
                }
                
                BottomSheet(
                    state = playerSheetState,
                    modifier = Modifier.fillMaxSize(),
                    collapsedBackgroundColor = playerColorScheme.surfaceContainer,
                    collapsedContent = {
                        // Wrap MiniPlayer in the player theme so it matches the ExpandedPlayer
                        MaterialTheme(colorScheme = playerColorScheme) {
                            MiniPlayerContent(
                                currentSong = currentSong,
                                position = playbackState.currentPosition,
                                duration = playbackState.duration,
                                isPlaying = playbackState.isPlaying,
                                onPlayPauseClick = { nowPlayingViewModel.togglePlayPause() },
                                onFavoriteClick = { nowPlayingViewModel.toggleFavorite() }
                            )
                        }
                    },
                    content = {
                        ExpandedPlayerContent(
                            currentSong = currentSong,
                            queue = playbackState.queue,
                            currentSongIndex = playbackState.currentIndex,
                            isPlaying = playbackState.isPlaying,
                            position = playbackState.currentPosition,
                            duration = playbackState.duration,
                            shuffleEnabled = playbackState.shuffleEnabled,
                            repeatMode = playbackState.repeatMode,
                            onCollapseClick = { playerSheetState.collapseSoft() },
                            onPlayPauseClick = { nowPlayingViewModel.togglePlayPause() },
                            onNextClick = { nowPlayingViewModel.playNext() },
                            onPreviousClick = { nowPlayingViewModel.playPrevious() },
                            onFavoriteClick = { nowPlayingViewModel.toggleFavorite() },
                            onSeek = { nowPlayingViewModel.seekTo(it) },
                            onShuffleClick = { nowPlayingViewModel.toggleShuffle() },
                            onRepeatClick = { nowPlayingViewModel.toggleRepeat() },
 
 
                            onToggleLyrics = { settingsViewModel.setShowLyrics(!showLyrics) },
                            sliderStyle = sliderStyle,
                            // Lyrics settings
                            showLyrics = showLyrics,
                            lyricsTextPosition = lyricsTextPosition,
                            lyricsAnimationStyle = lyricsAnimationStyle,
                            lyricsGlowEffect = lyricsGlowEffect,
                            lyricsTextSize = lyricsTextSize,
                            lyricsLineSpacing = lyricsLineSpacing,
                            // Volume, Speed, Pitch controls
                            currentVolume = nowPlayingViewModel.getVolume(),
                            onVolumeChange = { nowPlayingViewModel.setVolume(it) },
                            currentSpeed = nowPlayingViewModel.getPlaybackSpeed(),
                            onSpeedChange = { nowPlayingViewModel.setPlaybackSpeed(it) },
                            currentPitch = nowPlayingViewModel.getPlaybackPitch(),
                            onPitchChange = { nowPlayingViewModel.setPlaybackPitch(it) },
                            audioSessionId = nowPlayingViewModel.getAudioSessionId(),
                            onAddToPlaylist = { showAddToPlaylistSheet = true },
                            onQueueItemClick = { index -> nowPlayingViewModel.skipToQueueItem(index) },
                            onAlbumClick = {
                                val albumId = currentSong.albumId ?: return@ExpandedPlayerContent
                                playerSheetState.collapseSoft()
                                navController.navigate(Screen.Album.createRoute(albumId))
                            },
                            isDarkTheme = isDarkTheme,
                            isPureBlack = isPureBlack,
                            seekIntervalSeconds = seekInterval,
                            swipeGesturesEnabled = swipeGestures,
                            playerBackground = playerBackground
                        )
                    }
                )
            }
            
            // Add to Playlist Sheet
            if (showAddToPlaylistSheet && hasCurrentSong) {
                com.ngt.pixplay.ui.components.AddToPlaylistSheet(
                    playlists = playlists,
                    onPlaylistClick = { playlistId ->
                        playlistViewModel.addSongToPlaylist(playlistId, playbackState.currentSong!!.id)
                        Toast.makeText(context, "Added to playlist", Toast.LENGTH_SHORT).show()
                        showAddToPlaylistSheet = false
                    },
                    onCreateNewClick = {
                        showCreatePlaylistDialog = true
                    },
                    onDismissRequest = { showAddToPlaylistSheet = false }
                )
            }
 
            // Create Playlist Dialog
            if (showCreatePlaylistDialog) {
                com.ngt.pixplay.ui.components.CreatePlaylistDialog(
                    onDismissRequest = { showCreatePlaylistDialog = false },
                    onConfirm = { name ->
                        playlistViewModel.createPlaylist(name)
                        // Optional: automatically add song to new playlist 
                        // But need the ID of the new playlist, which createPlaylist doesn't return immediately via flow or callback here easily.
                        // For now just create.
                        showCreatePlaylistDialog = false
                    }
                )
            }
            
            // Navigation bar - slides down simultaneously with player expansion
            // For detail screens, use animated transition
            val detailScreenOffset by androidx.compose.animation.core.animateDpAsState(
                targetValue = if (isDetailScreen) bottomInset + NavigationBarHeight else 0.dp,
                animationSpec = androidx.compose.animation.core.spring(
                    dampingRatio = androidx.compose.animation.core.Spring.DampingRatioNoBouncy,
                    stiffness = androidx.compose.animation.core.Spring.StiffnessMediumLow
                ),
                label = "detailScreenOffset"
            )
            
            NavigationBar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .height(bottomInset + NavigationBarHeight)
                    .offset {
                        // Use player progress directly for simultaneous animation
                        val playerOffset = (bottomInset + NavigationBarHeight) * 
                            playerSheetState.progress.coerceIn(0f, 1f)
                        // Take the max of player offset and detail screen offset
                        val totalOffset = maxOf(
                            with(density) { playerOffset.toPx() },
                            with(density) { detailScreenOffset.toPx() }
                        )
                        IntOffset(x = 0, y = totalOffset.roundToInt())
                    }
            ) {
                NavigationBarItem(
                    icon = {
                        Icon(
                            if (selectedItem == 0) Icons.Filled.Explore
                            else Icons.Outlined.Explore,
                            contentDescription = "Discover"
                        )
                    },
                    label = { Text("Discover") },
                    selected = selectedItem == 0,
                    onClick = {
                        if (playerSheetState.isExpanded) {
                            playerSheetState.collapseSoft()
                        }
                        selectedItem = 0
                        navController.navigate(Screen.Discover.route) {
                            popUpTo(Screen.Discover.route) { inclusive = true }
                        }
                    }
                )

                NavigationBarItem(
                    icon = {
                        Icon(
                            if (selectedItem == 1) Icons.Filled.Search
                            else Icons.Filled.Search, // Or different icon
                            contentDescription = "Search"
                        )
                    },
                    label = { Text("Search") },
                    selected = selectedItem == 1,
                    onClick = {
                        if (playerSheetState.isExpanded) {
                            playerSheetState.collapseSoft()
                        }
                        selectedItem = 1
                        navController.navigate(Screen.Search.route) {
                            popUpTo(Screen.Discover.route)
                        }
                    }
                )

                NavigationBarItem(
                    icon = {
                        Icon(
                            if (selectedItem == 2) Icons.Filled.LibraryMusic
                            else Icons.Outlined.LibraryMusic,
                            contentDescription = "Library"
                        )
                    },
                    label = { Text("Library") },
                    selected = selectedItem == 2,
                    onClick = {
                        if (playerSheetState.isExpanded) {
                            playerSheetState.collapseSoft()
                        }
                        selectedItem = 2
                        navController.navigate(Screen.Library.route) {
                            popUpTo(Screen.Discover.route)
                        }
                    }
                )
            }
        }
    }
}
