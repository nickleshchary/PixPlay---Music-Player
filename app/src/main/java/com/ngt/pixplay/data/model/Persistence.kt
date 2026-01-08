package com.ngt.pixplay.data.model

import java.io.Serializable

data class PersistPlayerState(
    val currentSongId: Long? = null,
    val currentIndex: Int = -1,
    val currentPosition: Long = 0,
    val queue: List<AudioItem> = emptyList(),
    val shuffleEnabled: Boolean = false,
    val repeatMode: Int = 0
) : Serializable {
    companion object {
        private const val serialVersionUID = 1L
    }
}
