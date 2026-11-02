package com.neighborhero.neighborhero.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.neighborhero.neighborhero.data.network.ApiConfig
import com.neighborhero.neighborhero.data.network.NetworkClient
import com.neighborhero.neighborhero.ui.theme.SurfaceContainerLow
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
    var url = rawUrl.trim()
    val apiHost = try {
        java.net.URI(ApiConfig.baseUrl).host
    } catch (_: Exception) {
        null
    }

    if (apiHost != null) {
        if (url.startsWith("http://localhost") || url.startsWith("https://localhost")) {
            url = url.replaceFirst("localhost", apiHost)
        } else if (url.startsWith("http://127.0.0.1") || url.startsWith("https://127.0.0.1")) {
            url = url.replaceFirst("127.0.0.1", apiHost)
        } else if (!ApiConfig.isEmulator() && (url.startsWith("http://10.0.2.2") || url.startsWith("https://10.0.2.2"))) {
            url = url.replaceFirst("10.0.2.2", apiHost)
        }
    }

    if (url.startsWith("http://") || url.startsWith("https://")) {
        return url
    }
    val clean = url.trimStart('/')
    val base = ApiConfig.baseUrl.removeSuffix("api/")
    return "$base$clean"
}

@Composable
fun NetworkImage(
    url: String?,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    contentScale: ContentScale = ContentScale.Crop,
    loadingContent: (@Composable () -> Unit)? = null,
    errorContent: (@Composable () -> Unit)? = null,
    fallback: @Composable () -> Unit
) {
    val fullUrl = remember(url) { resolveImageUrl(url) }
    var bitmap by remember(fullUrl) { mutableStateOf<Bitmap?>(fullUrl?.let { ImageCache.get(it) }) }
    var isLoading by remember(fullUrl) { mutableStateOf(!fullUrl.isNullOrBlank() && bitmap == null) }
    var isError by remember(fullUrl) { mutableStateOf(false) }

    LaunchedEffect(fullUrl) {
        if (fullUrl.isNullOrBlank()) {
            bitmap = null
            isLoading = false
            isError = false
            return@LaunchedEffect
        }
        val cached = ImageCache.get(fullUrl)
        if (cached != null) {
            bitmap = cached
            isLoading = false
            isError = false
            return@LaunchedEffect
        }

        isLoading = true
        isError = false

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
                                isLoading = false
                                isError = false
                            }
                            return@withContext
                        }
                    }
                }
                withContext(Dispatchers.Main) {
                    isLoading = false
                    isError = true
                }
            } catch (_: Exception) {
                withContext(Dispatchers.Main) {
                    isLoading = false
                    isError = true
                }
            }
        }
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        when {
            bitmap != null -> {
                Image(
                    bitmap = bitmap!!.asImageBitmap(),
                    contentDescription = contentDescription,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = contentScale
                )
            }
            isLoading -> {
                if (loadingContent != null) {
                    loadingContent()
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(SurfaceContainerLow),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(28.dp),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 2.5.dp
                        )
                    }
                }
            }
            isError -> {
                if (errorContent != null) {
                    errorContent()
                } else {
                    fallback()
                }
            }
            else -> {
                fallback()
            }
        }
    }
}
