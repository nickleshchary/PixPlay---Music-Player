package com.ngt.pixplay.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing an audio track with all metadata
 */
@Entity(tableName = "songs")
data class AudioItem(
    @PrimaryKey val id: Long,
    val title: String,
    val artist: String,
    val artistId: Long?,
    val album: String,
    val albumId: Long?,
    val duration: Long, // in milliseconds
    val path: String, // file path
    val albumArtUri: String?, // content URI for album art
    val dateAdded: Long,
    val dateModified: Long,
    val size: Long, // file size in bytes
    val mimeType: String,
    val isFavorite: Boolean = false,
    val playCount: Int = 0,
    val lastPlayed: Long = 0,
    val lyrics: String? = null
) : java.io.Serializable {
    companion object {
        private const val serialVersionUID = 1L
    }
}
