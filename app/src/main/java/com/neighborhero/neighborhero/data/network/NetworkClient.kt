package com.neighborhero.neighborhero.data.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkClient {

    private const val CONNECT_TIMEOUT_SECONDS = 15L
    private const val READ_TIMEOUT_SECONDS = 20L
    private const val WRITE_TIMEOUT_SECONDS = 20L

    @Volatile
    private var apiService: NeighborHeroApiService? = null

    @Volatile
    private var customClient: NeighborHeroApiService? = null

    @Volatile
    private var okHttpClientInstance: OkHttpClient? = null

    fun getOkHttpClient(): OkHttpClient {
        return okHttpClientInstance ?: synchronized(this) {
            okHttpClientInstance ?: buildOkHttpClient().also { okHttpClientInstance = it }
        }
    }

    fun getService(): NeighborHeroApiService {
        return customClient ?: apiService ?: synchronized(this) {
            apiService ?: buildService().also { apiService = it }
        }
    }

    fun setTestService(service: NeighborHeroApiService?) {
        customClient = service
    }

    fun resetClient() {
        synchronized(this) {
            apiService = null
            okHttpClientInstance = null
        }
    }

    private fun buildOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(SessionManager.getInstance()))
            .addInterceptor(loggingInterceptor)
            .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    private fun buildService(): NeighborHeroApiService {
        val retrofit = Retrofit.Builder()
            .baseUrl(ApiConfig.baseUrl)
            .client(getOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(NeighborHeroApiService::class.java)
    }
}
