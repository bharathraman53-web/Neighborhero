package com.neighborhero.neighborhero

import android.app.Application
import android.util.Log
import com.neighborhero.neighborhero.data.network.ApiConfig
import com.neighborhero.neighborhero.data.repository.RepositoryProvider

class NeighborHeroApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        RepositoryProvider.init(this)
        Log.i("NeighborHeroApp", "API configured at: ${ApiConfig.baseUrl} (isEmulator=${ApiConfig.isEmulator()})")
    }
}
