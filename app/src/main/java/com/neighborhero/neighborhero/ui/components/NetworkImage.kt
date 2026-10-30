package com.neighborhero.neighborhero.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import com.neighborhero.neighborhero.data.network.ApiConfig
import com.neighborhero.neighborhero.data.network.NetworkClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request

object ImageCache {
    private val memoryCache = object : LruCache<String, Bitmap>(50 * 1024 * 1024) {
        override fun sizeOf(key: String, value: Bitmap): Int {
            return value.byteCount
        }
    }

    fun get(url: String): Bitmap? = synchronized(this) { memoryCache.get(url) }

    fun put(url: String, bitmap: Bitmap) {
        synchronized(this) { memoryCache.put(url, bitmap) }
    }

    fun evict(url: String) {
        synchronized(this) { memoryCache.remove(url) }
    }
}

fun resolveImageUrl(rawUrl: String?): String? {
    if (rawUrl.isNullOrBlank()) return null
    if (rawUrl.startsWith("http://") || rawUrl.startsWith("https://")) {
        return rawUrl
    }
    val clean = rawUrl.trimStart('/')
    val base = ApiConfig.baseUrl.removeSuffix("api/")
    return "$base$clean"
}

@Composable
fun NetworkImage(
    url: String?,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    contentScale: ContentScale = ContentScale.Crop,
    fallback: @Composable () -> Unit
) {
    val fullUrl = remember(url) { resolveImageUrl(url) }
    var bitmap by remember(fullUrl) { mutableStateOf<Bitmap?>(fullUrl?.let { ImageCache.get(it) }) }

    LaunchedEffect(fullUrl) {
        if (fullUrl.isNullOrBlank()) {
            bitmap = null
            return@LaunchedEffect
        }
        val cached = ImageCache.get(fullUrl)
        if (cached != null) {
            bitmap = cached
            return@LaunchedEffect
        }
        withContext(Dispatchers.IO) {
            try {
                val client = NetworkClient.getOkHttpClient()
                val request = Request.Builder().url(fullUrl).build()
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val bytes = response.body?.bytes()
                    if (bytes != null && bytes.isNotEmpty()) {
                        val decoded = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        if (decoded != null) {
                            ImageCache.put(fullUrl, decoded)
                            withContext(Dispatchers.Main) {
                                bitmap = decoded
                            }
                        }
                    }
                }
            } catch (_: Exception) {
                // Keep fallback on network error
            }
        }
    }

    if (bitmap != null) {
        Image(
            bitmap = bitmap!!.asImageBitmap(),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale
        )
    } else {
        fallback()
    }
}

