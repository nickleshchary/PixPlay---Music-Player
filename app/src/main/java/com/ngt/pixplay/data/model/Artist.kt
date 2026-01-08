package com.ngt.pixplay.data.model

/**
 * Data class representing an artist with their songs and albums
 */
data class Artist(
    val id: Long,
    val name: String,
    val albumCount: Int,
    val songCount: Int,
    val songs: List<AudioItem> = emptyList(),
    val albums: List<Album> = emptyList()
)
