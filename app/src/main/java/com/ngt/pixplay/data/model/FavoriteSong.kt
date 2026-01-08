package com.ngt.pixplay.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity to persist song favorite status across library refreshes
 */
@Entity(tableName = "favorite_songs")
data class FavoriteSong(
    @PrimaryKey val songId: Long,
    val dateAdded: Long = System.currentTimeMillis()
)
