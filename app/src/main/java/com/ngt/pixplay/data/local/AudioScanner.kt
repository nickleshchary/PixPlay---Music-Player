package com.ngt.pixplay.data.local

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.ngt.pixplay.data.model.AudioItem
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File // For file operations
import java.util.logging.Level
import java.util.logging.Logger
import javax.inject.Inject
import javax.inject.Singleton
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey

/**
 * Scans device storage for audio files using MediaStore API
 */
@Singleton
class AudioScanner @Inject constructor(
    @ApplicationContext private val context: Context
) {
    init {
        // Disable Jaudiotagger logging
        Logger.getLogger("org.jaudiotagger").level = Level.OFF
    }

    suspend fun scanAudioFiles(): List<AudioItem> = withContext(Dispatchers.IO) {
        val audioList = mutableListOf<AudioItem>()
        
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }
        
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ARTIST_ID,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA, // File path
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.DATE_MODIFIED,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.MIME_TYPE
        )
        
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"
        
        context.contentResolver.query(
            collection,
            projection,
            selection,
            null,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val artistIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST_ID)
            val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
            val dateModifiedColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_MODIFIED)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
            val mimeTypeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE)
            
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val title = cursor.getString(titleColumn) ?: "Unknown"
                val artist = cursor.getString(artistColumn) ?: "Unknown Artist"
                val artistId = cursor.getLongOrNull(artistIdColumn)
                val album = cursor.getString(albumColumn) ?: "Unknown Album"
                val albumId = cursor.getLongOrNull(albumIdColumn)
                val duration = cursor.getLong(durationColumn)
                val path = cursor.getString(dataColumn) ?: ""
                val dateAdded = cursor.getLong(dateAddedColumn)
                val dateModified = cursor.getLong(dateModifiedColumn)
                val size = cursor.getLong(sizeColumn)
                val mimeType = cursor.getString(mimeTypeColumn) ?: "audio/*"
                
                // Get album art URI
                val albumArtUri = albumId?.let {
                    ContentUris.withAppendedId(
                        Uri.parse("content://media/external/audio/albumart"),
                        it
                    ).toString()
                }
                
                // Try to simple read .lrc file if exists
                val lyrics = getLyricsForFile(path)
                
                val audioItem = AudioItem(
                    id = id,
                    title = title,
                    artist = artist,
                    artistId = artistId,
                    album = album,
                    albumId = albumId,
                    duration = duration,
                    path = path,
                    albumArtUri = albumArtUri,
                    dateAdded = dateAdded,
                    dateModified = dateModified,
                    size = size,
                    mimeType = mimeType,
                    lyrics = lyrics
                )
                
                audioList.add(audioItem)
            }
        }
        
        audioList
    }
    
    private fun android.database.Cursor.getLongOrNull(columnIndex: Int): Long? {
        return if (isNull(columnIndex)) null else getLong(columnIndex)
    }
    
    private fun getLyricsForFile(audioPath: String): String? {
        try {
            if (audioPath.isBlank()) return null
            
            val audioFile = File(audioPath)
            val parent = audioFile.parentFile ?: return null
            val nameInfo = audioFile.nameWithoutExtension
            
            val lrcFile = File(parent, "$nameInfo.lrc")
            if (lrcFile.exists() && lrcFile.canRead()) {
                return lrcFile.readText()
            }
            
            // If no .lrc file, try embedded lyrics
            try {
                // AudioFileIO.read requires a File object.
                // On Android 10+, this might fail if we don't have direct access,
                // but since we're scanning MEDIA_DATA which gives a path, it works if we have permission.
                val audioFileObj = AudioFileIO.read(audioFile)
                val tag = audioFileObj.tag
                if (tag != null) {
                    return tag.getFirst(FieldKey.LYRICS)
                }
            } catch (e: Exception) {
                // Ignore errors reading tags
            }
            
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
    fun deleteAudio(audioIds: List<Long>): android.app.PendingIntent? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val uris = audioIds.map { 
                ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, it)
            }
            return MediaStore.createDeleteRequest(context.contentResolver, uris)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
             try {
                 // For Android 10, we try to delete 1 by 1 or throws RecoverableSecurityException
                 // Since createDeleteRequest is R+, for Q we need to catch exception.
                 // This method returns PendingIntent only for R+. 
                 // For Q, the calling code needs to handle exception.
                 // So we should probably refactor this to either return IntentSender or throw.
                 // To keep it simple for now, let's implement a 'safeDelete' that throws specific events?
                 // Or better: Let's follow the standard: return IntentSenderRequest?
                 return null 
             } catch (e: Exception) {
                 return null
             }
        }
        return null
    }
    
    // Improved delete method that returns an IntentSender for handling permissions
    fun getDeleteIntent(audioIds: List<Long>): android.content.IntentSender? {
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val uris = audioIds.map { 
                ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, it)
            }
            return MediaStore.createDeleteRequest(context.contentResolver, uris).intentSender
        }
        return null
    }

    // Direct delete for Pre-Q or when we have permission
    // Returns true if deleted, false if permission needed (throws on Q)
    fun deleteAudioDirectly(audioId: Long): Boolean {
        val uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, audioId)
        try {
            val rows = context.contentResolver.delete(uri, null, null)
            return rows > 0
        } catch (e: android.app.RecoverableSecurityException) {
            throw e
        } catch (e: SecurityException) {
            // On R+, this is thrown if we don't have permission and haven't requested it via createDeleteRequest
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                throw e
            }
        }
        return false
    }
}
