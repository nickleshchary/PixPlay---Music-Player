package com.ngt.pixplay.data.model

/**
 * Data class representing an album with its songs
 */
data class Album(
    val id: Long,
    val name: String,
    val artist: String,
    val artistId: Long?,
    val albumArtUri: String?,
    val songCount: Int,
    val songs: List<AudioItem> = emptyList(),
    val year: Int? = null,
    val isFavorite: Boolean = false
)
