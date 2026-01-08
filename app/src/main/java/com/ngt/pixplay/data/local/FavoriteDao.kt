package com.ngt.pixplay.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ngt.pixplay.data.model.FavoriteAlbum
import com.ngt.pixplay.data.model.FavoriteSong
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    // Song Favorites
    @Query("SELECT songId FROM favorite_songs")
    suspend fun getAllFavoriteSongIds(): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavoriteSong(favoriteSong: FavoriteSong)

    @Query("DELETE FROM favorite_songs WHERE songId = :songId")
    suspend fun deleteFavoriteSong(songId: Long)

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_songs WHERE songId = :songId)")
    suspend fun isSongFavorite(songId: Long): Boolean

    // Album Favorites
    @Query("SELECT albumId FROM favorite_albums")
    fun getFavoriteAlbumIdsFlow(): Flow<List<Long>>
    
    @Query("SELECT albumId FROM favorite_albums")
    suspend fun getAllFavoriteAlbumIds(): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavoriteAlbum(favoriteAlbum: FavoriteAlbum)

    @Query("DELETE FROM favorite_albums WHERE albumId = :albumId")
    suspend fun deleteFavoriteAlbum(albumId: Long)

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_albums WHERE albumId = :albumId)")
    suspend fun isAlbumFavorite(albumId: Long): Boolean
}
