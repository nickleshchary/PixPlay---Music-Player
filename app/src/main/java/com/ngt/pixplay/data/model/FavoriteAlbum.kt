package com.ngt.pixplay.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity to persist album favorite status across library refreshes
 */
@Entity(tableName = "favorite_albums")
data class FavoriteAlbum(
    @PrimaryKey val albumId: Long,
    val dateAdded: Long = System.currentTimeMillis()
)
