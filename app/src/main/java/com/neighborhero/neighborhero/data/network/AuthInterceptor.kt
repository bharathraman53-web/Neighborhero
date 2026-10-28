package com.neighborhero.neighborhero.data.network

import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val sessionManager: SessionManager = SessionManager.getInstance()
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val builder = original.newBuilder()

        // Attach bearer token if authenticated
        val token = sessionManager.getToken()
        if (!token.isNullOrBlank()) {
            builder.header("Authorization", "Bearer $token")
        }

        builder.header("Accept", "application/json")

        val response = chain.proceed(builder.build())

        // Handle expired session / 401 Unauthorized
        if (response.code == 401) {
            sessionManager.clearSession()
        }

        return response
    }
}
