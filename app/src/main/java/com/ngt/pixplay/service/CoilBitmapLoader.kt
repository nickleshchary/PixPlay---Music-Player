package com.ngt.pixplay.service

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.graphics.createBitmap
import androidx.media3.common.util.BitmapLoader
import coil3.imageLoader
import coil3.request.ErrorResult
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowHardware
import coil3.toBitmap
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.SettableFuture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CoilBitmapLoader(
    private val context: Context,
    private val scope: CoroutineScope,
) : BitmapLoader {
    override fun supportsMimeType(mimeType: String): Boolean = mimeType.startsWith("image/")

    override fun decodeBitmap(data: ByteArray): ListenableFuture<Bitmap> {
        val future = SettableFuture.create<Bitmap>()
        scope.launch(Dispatchers.IO) {
            try {
                val bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
                    ?: createBitmap(64, 64) // Return fallback bitmap instead of throwing error
                future.set(bitmap)
            } catch (e: Exception) {
                // Handle bitmap decode errors gracefully
                android.util.Log.w("CoilBitmapLoader", "Failed to decode bitmap data", e)
                future.set(createBitmap(64, 64)) // Return fallback bitmap
            }
        }
        return future
    }

    override fun loadBitmap(uri: Uri): ListenableFuture<Bitmap> {
        val future = SettableFuture.create<Bitmap>()
        scope.launch(Dispatchers.IO) {
            try {
                val request = ImageRequest.Builder(context)
                    .data(uri)
                    .allowHardware(false)
                    .build()

                val result = context.imageLoader.execute(request)

                // In case of error, returns an empty bitmap
                when (result) {
                    is ErrorResult -> {
                        future.set(createBitmap(64, 64))
                    }
                    is SuccessResult -> {
                        try {
                            future.set(result.image.toBitmap())
                        } catch (e: Exception) {
                            future.set(createBitmap(64, 64))
                        }
                    }
                }
            } catch (e: Exception) {
                future.set(createBitmap(64, 64))
            }
        }
        return future
    }
}
