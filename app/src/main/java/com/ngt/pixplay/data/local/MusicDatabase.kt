package com.ngt.pixplay.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ngt.pixplay.data.model.AudioItem
import com.ngt.pixplay.data.model.FavoriteAlbum
import com.ngt.pixplay.data.model.FavoriteSong
import com.ngt.pixplay.data.model.Playlist

@Database(
    entities = [AudioItem::class, Playlist::class, FavoriteSong::class, FavoriteAlbum::class],
    version = 3,
    exportSchema = false
)
abstract class MusicDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun favoriteDao(): FavoriteDao
    
    companion object {
        const val DATABASE_NAME = "pixplay_music.db"
    }
}
