package com.ngt.pixplay.data.repository

import com.ngt.pixplay.data.local.AudioScanner
import com.ngt.pixplay.data.local.MusicDatabase
import com.ngt.pixplay.data.model.Album
import com.ngt.pixplay.data.model.Artist
import com.ngt.pixplay.data.model.AudioItem
import com.ngt.pixplay.data.model.Playlist
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for music data operations
 */
@Singleton
class MusicRepository @Inject constructor(
    private val database: MusicDatabase,
    private val audioScanner: AudioScanner
) {
    private val songDao = database.songDao()
    private val playlistDao = database.playlistDao()
    private val favoriteDao = database.favoriteDao()
    
    // Songs
    fun getAllSongs(): Flow<List<AudioItem>> = songDao.getAllSongs()
    
    fun getFavoriteSongs(): Flow<List<AudioItem>> = songDao.getFavoriteSongs()
    
    fun getRecentlyPlayed(limit: Int = 20): Flow<List<AudioItem>> = songDao.getRecentlyPlayed(limit)
    
    fun getReconnectSongs(limit: Int = 20): Flow<List<AudioItem>> {
        // Reduced threshold to 1 play to show content earlier
        val thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
        
        return kotlinx.coroutines.flow.combine(
            songDao.getReconnectSongs(cutoffTimestamp = thirtyDaysAgo, limit = limit),
            songDao.getRandomSongs(limit = limit)
        ) { reconnect, randomFallback ->
            if (reconnect.isNotEmpty()) reconnect else randomFallback
        }
    }

    fun getMostPlayedSongs(limit: Int = 20): Flow<List<AudioItem>> {
        return kotlinx.coroutines.flow.combine(
            songDao.getMostPlayedSongs(limit),
            songDao.getRandomSongs(limit = limit)
        ) { mostPlayed, randomFallback ->
             if (mostPlayed.isNotEmpty()) mostPlayed else randomFallback
        }
    }
    
    // Recently Added usually strictly means "Recently Added", fallback might be confusing but user asked for "no empty sections".
    // For now, I'll leave Recently Added strictly as Recently Added unless user complains, or I can just fallback same way.
    // User said "every sections". Let's do it for Recently Added too?
    // Actually "Recently Added" with fallback to "Random" is weird. "Recently Added" implies chronology.
    // If you have ANY songs, you have recently added songs (all of them are added at some point). 
    // If you have NO songs, then random is empty anyway.
    // So getRecentlyAddedSongs doesn't need fallback because if it's empty, the library is empty.
    
    fun getRecentlyAddedSongs(limit: Int = 20): Flow<List<AudioItem>> = songDao.getRecentlyAddedSongs(limit)

    fun getForYouTracks(limit: Int = 20): Flow<List<AudioItem>> {
        // "For You" = Mix of recent habits (Recently Played) and familiarity (Random Favorites)
        return kotlinx.coroutines.flow.combine(
            songDao.getRecentlyPlayed(limit = limit / 2),
            songDao.getRandomFavorites(limit = limit / 2),
            songDao.getRandomSongs(limit = limit)
        ) { recent, favorites, randomFallback ->
            val mixed = (recent + favorites).distinctBy { it.id }.shuffled()
            if (mixed.isNotEmpty()) mixed else randomFallback
        }
    }
    
    suspend fun getSongById(id: Long): AudioItem? = songDao.getSongById(id)
    
    fun getSongsByIds(ids: List<Long>): Flow<List<AudioItem>> = songDao.getSongsByIds(ids)
    
    fun searchSongs(query: String): Flow<List<AudioItem>> = songDao.searchSongs(query)

    fun searchAlbums(query: String): Flow<List<Album>> {
        // We reuse searchSongs but filter specifically for album name matches for better relevance
        // or we can allow loose matching. For now, let's group the search results.
        return songDao.searchSongs(query).map { songs ->
            songs.groupBy { it.albumId to it.album }
                .map { (albumInfo, albumSongs) ->
                    val (albumId, albumName) = albumInfo
                    Album(
                        id = albumId ?: -1L,
                        name = albumName,
                        artist = albumSongs.firstOrNull()?.artist ?: "Unknown",
                        artistId = albumSongs.firstOrNull()?.artistId,
                        albumArtUri = albumSongs.firstOrNull()?.albumArtUri,
                        songCount = albumSongs.size,
                        songs = albumSongs
                    )
                }
                .filter { it.name.contains(query, ignoreCase = true) || it.artist.contains(query, ignoreCase = true) }
                .sortedBy { it.name }
        }
    }
    
    suspend fun toggleFavorite(songId: Long, isFavorite: Boolean) {
        // Update cache
        songDao.updateFavoriteStatus(songId, isFavorite)
        
        // Update persistence
        if (isFavorite) {
            favoriteDao.insertFavoriteSong(com.ngt.pixplay.data.model.FavoriteSong(songId))
        } else {
            favoriteDao.deleteFavoriteSong(songId)
        }
    }
    
    suspend fun toggleAlbumFavorite(albumId: Long, isFavorite: Boolean) {
        // Only update persistence - separate from song favorites
        if (isFavorite) {
            favoriteDao.insertFavoriteAlbum(com.ngt.pixplay.data.model.FavoriteAlbum(albumId))
        } else {
            favoriteDao.deleteFavoriteAlbum(albumId)
        }
    }
    
    suspend fun incrementPlayCount(songId: Long) {
        songDao.incrementPlayCount(songId, System.currentTimeMillis())
    }
    
    // Albums
    fun getAllAlbums(): Flow<List<Album>> {
        return kotlinx.coroutines.flow.combine(
            songDao.getAllSongs(),
            favoriteDao.getFavoriteAlbumIdsFlow()
        ) { songs, favoriteAlbumIds ->
            songs.groupBy { it.albumId to it.album }
                .map { (albumInfo, albumSongs) ->
                    val (albumId, albumName) = albumInfo
                    val realAlbumId = albumId ?: -1L
                    Album(
                        id = realAlbumId,
                        name = albumName,
                        artist = albumSongs.firstOrNull()?.artist ?: "Unknown",
                        artistId = albumSongs.firstOrNull()?.artistId,
                        albumArtUri = albumSongs.firstOrNull()?.albumArtUri,
                        songCount = albumSongs.size,
                        songs = albumSongs,
                        // Album is favored if it's in the favorite albums table
                        isFavorite = favoriteAlbumIds.contains(realAlbumId)
                    )
                }
                .sortedBy { it.name }
        }
    }

    fun getTopAlbums(limit: Int = 10): Flow<List<Album>> {
        return kotlinx.coroutines.flow.combine(
            songDao.getAllSongs(),
            favoriteDao.getFavoriteAlbumIdsFlow()
        ) { songs, favoriteAlbumIds ->
            songs.groupBy { it.albumId to it.album }
                .mapNotNull { (albumInfo, albumSongs) ->
                    val (albumId, albumName) = albumInfo
                    val realAlbumId = albumId ?: return@mapNotNull null

                    // Calculate stats
                    val totalPlays = albumSongs.sumOf { it.playCount }
                    
                    // Score = Total Plays * Completion Rate
                    // For unplayed albums, this will simply be 0
                    val score = if (totalPlays > 0) {
                        val uniqueTracksPlayed = albumSongs.count { it.playCount > 0 }
                        val totalTracks = albumSongs.size
                        val completionRate = if (totalTracks > 0) uniqueTracksPlayed.toFloat() / totalTracks else 0f
                        totalPlays * completionRate
                    } else {
                        0f
                    }

                    val album = Album(
                        id = realAlbumId,
                        name = albumName,
                        artist = albumSongs.firstOrNull()?.artist ?: "Unknown",
                        artistId = albumSongs.firstOrNull()?.artistId,
                        albumArtUri = albumSongs.firstOrNull()?.albumArtUri,
                        songCount = albumSongs.size,
                        songs = albumSongs,
                        isFavorite = favoriteAlbumIds.contains(realAlbumId)
                    )
                    
                    album to score
                }
                .sortedWith(compareByDescending<Pair<Album, Float>> { it.second }
                    .thenBy { it.first.name })
                .take(limit)
                .map { it.first }
        }
    }
    
    suspend fun getAlbumById(albumId: Long): Album? {
        // This is still a placeholder/buggy in original code, but I'll leave it as is 
        // or fix it if I can. The user didn't ask to fix this specific method, 
        // but getAllAlbums is what feeds the UI.
        return null // TODO: Implement proper album retrieval
    }
    
    // ... items ...

    // Scanning
    suspend fun scanAndSaveAudioFiles() {
        val audioFiles = audioScanner.scanAudioFiles()
        
        // Get valid favorite song IDs from persistence
        val favoriteIds = favoriteDao.getAllFavoriteSongIds().toSet()
        
        // Apply favorite status to scanned files
        val mergedFiles = audioFiles.map { audio ->
            if (favoriteIds.contains(audio.id)) {
                audio.copy(isFavorite = true)
            } else {
                audio
            }
        }
        
        songDao.insertSongs(mergedFiles)
    }
    fun getAllArtists(): Flow<List<Artist>> {
        return songDao.getAllSongs().map { songs ->
            songs.groupBy { it.artistId to it.artist }
                .map { (artistInfo, artistSongs) ->
                    val (artistId, artistName) = artistInfo
                    val albums = artistSongs.groupBy { it.albumId }.size
                    
                    Artist(
                        id = artistId ?: -1,
                        name = artistName,
                        albumCount = albums,
                        songCount = artistSongs.size,
                        songs = artistSongs
                    )
                }
                .sortedBy { it.name }
        }
    }
    
    // Playlists
    fun getAllPlaylists(): Flow<List<Playlist>> = playlistDao.getAllPlaylists()
    
    suspend fun getPlaylistById(id: Long): Playlist? = playlistDao.getPlaylistById(id)
    
    suspend fun createPlaylist(name: String): Long {
        val playlist = Playlist(
            name = name,
            createdAt = System.currentTimeMillis(),
            modifiedAt = System.currentTimeMillis()
        )
        return playlistDao.insertPlaylist(playlist)
    }
    
    suspend fun addSongToPlaylist(playlistId: Long, songId: Long) {
        val playlist = playlistDao.getPlaylistById(playlistId) ?: return
        val currentIds = playlist.getSongIdsList().toMutableList()
        if (!currentIds.contains(songId)) {
            currentIds.add(songId)
            val updatedPlaylist = playlist.setSongIds(currentIds)
                .copy(modifiedAt = System.currentTimeMillis())
            playlistDao.updatePlaylist(updatedPlaylist)
        }
    }

    suspend fun addSongsToPlaylist(playlistId: Long, songIds: List<Long>) {
        val playlist = playlistDao.getPlaylistById(playlistId) ?: return
        val currentIds = playlist.getSongIdsList().toMutableList()
        var changed = false
        songIds.forEach { id ->
            if (!currentIds.contains(id)) {
                currentIds.add(id)
                changed = true
            }
        }
        if (changed) {
            val updatedPlaylist = playlist.setSongIds(currentIds)
                .copy(modifiedAt = System.currentTimeMillis())
            playlistDao.updatePlaylist(updatedPlaylist)
        }
    }
    
    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long) {
        val playlist = playlistDao.getPlaylistById(playlistId) ?: return
        val currentIds = playlist.getSongIdsList().toMutableList()
        currentIds.remove(songId)
        val updatedPlaylist = playlist.setSongIds(currentIds)
            .copy(modifiedAt = System.currentTimeMillis())
        playlistDao.updatePlaylist(updatedPlaylist)
    }
    
    suspend fun deletePlaylist(playlistId: Long) {
        playlistDao.deletePlaylistById(playlistId)
    }
    
    suspend fun renamePlaylist(playlistId: Long, newName: String) {
        val playlist = playlistDao.getPlaylistById(playlistId) ?: return
        val updatedPlaylist = playlist.copy(
            name = newName,
            modifiedAt = System.currentTimeMillis()
        )
        playlistDao.updatePlaylist(updatedPlaylist)
    }
    

    
    suspend fun refreshLibrary() {
        songDao.deleteAllSongs()
        scanAndSaveAudioFiles()
    }
    
    // Deletion Logic
    
    /**
     * Deletes songs from device. 
     * Returns IntentSender if permission is required (Android 10/11+), null if deleted successfully or failed without permission flow.
     * Throws RecoverableSecurityException on Android 10 if caught.
     */
    suspend fun deleteSongs(songs: List<AudioItem>): android.content.IntentSender? {
         // 1. Get IDs
         val ids = songs.map { it.id }
         
         // 2. Try Android R+ approach first
         if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
             return audioScanner.getDeleteIntent(ids)
         }
         
         // 3. For Q and below, try direct delete
         // Note: We only delete the FIRST one for now to trigger permission if needed, 
         // implementation for batch delete on Q with RecoverableSecurityException is complex loop.
         // We'll iterate.
         
         try {
             for (song in songs) {
                 audioScanner.deleteAudioDirectly(song.id)
                 // If successful, remove from DB
                songDao.deleteSongById(song.id) 
                 // Also clean up favorites/playlists if needed (Room usually handles Cascade if configured, otherwise do manually)
                 // Explicitly removing from favorites table just in case
                 favoriteDao.deleteFavoriteSong(song.id)
             }
         } catch (e: android.app.RecoverableSecurityException) {
             return e.userAction.actionIntent.intentSender
         } catch (e: SecurityException) {
             // Permission denied and no recovery?
             return null
         }
         
         return null
    }

    suspend fun deleteSong(song: AudioItem): android.content.IntentSender? {
        return deleteSongs(listOf(song))
    }
}
