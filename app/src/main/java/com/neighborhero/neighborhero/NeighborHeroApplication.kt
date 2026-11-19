package com.neighborhero.neighborhero

import android.app.Application
import android.content.Context
import android.util.Log
import com.neighborhero.neighborhero.data.network.ApiConfig
import com.neighborhero.neighborhero.data.repository.RepositoryProvider
import org.osmdroid.config.Configuration
import java.io.File

class NeighborHeroApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        RepositoryProvider.init(this)

        // Initialize OpenStreetMap (osmdroid) configuration
        try {
            val prefs = getSharedPreferences("osmdroid_prefs", Context.MODE_PRIVATE)
            val config = Configuration.getInstance()
            config.load(this, prefs)
            config.userAgentValue = "NeighborHero-Android-App/1.0 (${packageName})"
            config.osmdroidBasePath = File(cacheDir, "osmdroid")
            config.osmdroidTileCache = File(cacheDir, "osmdroid/tiles")

            // High-Performance Tile Rendering & Cache Optimizations:
            // 1. Expand RAM tile cache from default 9 to 48 tiles to avoid constant evictions and blurry re-rendering
            config.cacheMapTileCount = 48.toShort()
            // 2. Pre-cache adjacent ring of tiles to eliminate blank/blurred borders during panning
            config.cacheMapTileOvershoot = 2.toShort()
            // 3. Parallelize tile downloads and filesystem IO for instant high-res tile acquisition
            config.tileDownloadThreads = 4.toShort()
            config.tileFileSystemThreads = 4.toShort()
            config.tileDownloadMaxQueueSize = 60.toShort()
            config.tileFileSystemMaxQueueSize = 60.toShort()
            // 4. Set ample disk cache (100MB max, 80MB trim) to preserve downloaded high-res tiles across sessions
            config.tileFileSystemCacheMaxBytes = 100L * 1024L * 1024L
            config.tileFileSystemCacheTrimBytes = 80L * 1024L * 1024L
            // 5. Enable hardware canvas acceleration for smooth, unblurred rendering
            config.isMapViewHardwareAccelerated = true

            config.save(this, prefs)
        } catch (e: Exception) {
            Log.e("NeighborHeroApp", "Failed to configure osmdroid: ${e.message}")
        }

        Log.i("NeighborHeroApp", "API configured at: ${ApiConfig.baseUrl} (isEmulator=${ApiConfig.isEmulator()})")
    }
}
