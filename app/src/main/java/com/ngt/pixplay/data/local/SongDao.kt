package com.ngt.pixplay.data.local

import androidx.room.*
import com.ngt.pixplay.data.model.AudioItem
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {
    @Query("SELECT * FROM songs ORDER BY title ASC")
    fun getAllSongs(): Flow<List<AudioItem>>
    
    @Query("SELECT * FROM songs WHERE isFavorite = 1 ORDER BY lastPlayed DESC")
    fun getFavoriteSongs(): Flow<List<AudioItem>>
    
    @Query("SELECT * FROM songs ORDER BY lastPlayed DESC LIMIT :limit")
    fun getRecentlyPlayed(limit: Int = 20): Flow<List<AudioItem>>

    @Query("SELECT * FROM songs WHERE lastPlayed < :cutoffTimestamp ORDER BY lastPlayed ASC LIMIT :limit")
    fun getReconnectSongs(cutoffTimestamp: Long, limit: Int): Flow<List<AudioItem>>

    @Query("SELECT * FROM songs ORDER BY playCount DESC, title ASC LIMIT :limit")
    fun getMostPlayedSongs(limit: Int): Flow<List<AudioItem>>

    @Query("SELECT * FROM songs ORDER BY dateAdded DESC LIMIT :limit")
    fun getRecentlyAddedSongs(limit: Int): Flow<List<AudioItem>>

    @Query("SELECT * FROM songs WHERE isFavorite = 1 ORDER BY RANDOM() LIMIT :limit")
    fun getRandomFavorites(limit: Int): Flow<List<AudioItem>>

    @Query("SELECT * FROM songs ORDER BY RANDOM() LIMIT :limit")
    fun getRandomSongs(limit: Int): Flow<List<AudioItem>>

    
    @Query("SELECT * FROM songs WHERE id = :id")
    suspend fun getSongById(id: Long): AudioItem?
    
    @Query("SELECT * FROM songs WHERE id IN (:ids)")
    fun getSongsByIds(ids: List<Long>): Flow<List<AudioItem>>
    
    @Query("SELECT * FROM songs WHERE title LIKE '%' || :query || '%' OR artist LIKE '%' || :query || '%' OR album LIKE '%' || :query || '%'")
    fun searchSongs(query: String): Flow<List<AudioItem>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSongs(songs: List<AudioItem>)
    
    @Update
    suspend fun updateSong(song: AudioItem)
    
    @Query("UPDATE songs SET isFavorite = :isFavorite WHERE id = :songId")
    suspend fun updateFavoriteStatus(songId: Long, isFavorite: Boolean)
    
    @Query("UPDATE songs SET isFavorite = :isFavorite WHERE albumId = :albumId")
    suspend fun updateAlbumFavoriteStatus(albumId: Long, isFavorite: Boolean)
    
    @Query("UPDATE songs SET playCount = playCount + 1, lastPlayed = :timestamp WHERE id = :songId")
    suspend fun incrementPlayCount(songId: Long, timestamp: Long)
    
    @Query("DELETE FROM songs")
    suspend fun deleteAllSongs()
    
    @Query("DELETE FROM songs WHERE id = :id")
    suspend fun deleteSongById(id: Long)
}
