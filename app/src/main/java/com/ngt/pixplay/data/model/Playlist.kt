package com.ngt.pixplay.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing a user-created playlist
 */
@Entity(tableName = "playlists")
data class Playlist(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val createdAt: Long,
    val modifiedAt: Long,
    val songIds: String = "", // Comma-separated song IDs
    val coverArtUri: String? = null
) {
    fun getSongIdsList(): List<Long> {
        return if (songIds.isEmpty()) emptyList()
        else songIds.split(",").mapNotNull { it.toLongOrNull() }
    }
    
    fun setSongIds(ids: List<Long>): Playlist {
        return copy(songIds = ids.joinToString(","))
    }
}
