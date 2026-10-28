package com.neighborhero.neighborhero.data.network

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context? = null) {
    private val prefs: SharedPreferences? = context?.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "neighborhero_session"
        private const val KEY_TOKEN = "auth_bearer_token"
        private const val KEY_USER_ID = "authenticated_user_id"

        @Volatile
        private var instance: SessionManager? = null

        fun getInstance(context: Context? = null): SessionManager {
            return instance ?: synchronized(this) {
                instance ?: SessionManager(context?.applicationContext).also { instance = it }
            }
        }
    }

    // In-memory fallback if Context is null (e.g. in unit tests)
    private var inMemoryToken: String? = null
    private var inMemoryUserId: String? = null

    fun saveToken(token: String?) {
        inMemoryToken = token
        prefs?.edit()?.putString(KEY_TOKEN, token)?.apply()
    }

    fun getToken(): String? {
        return prefs?.getString(KEY_TOKEN, inMemoryToken) ?: inMemoryToken
    }

    fun saveUserId(userId: String?) {
        inMemoryUserId = userId
        prefs?.edit()?.putString(KEY_USER_ID, userId)?.apply()
    }

    fun getUserId(): String? {
        return prefs?.getString(KEY_USER_ID, inMemoryUserId) ?: inMemoryUserId
    }

    fun isLoggedIn(): Boolean {
        val t = getToken()
        return !t.isNullOrBlank()
    }

    fun clearSession() {
        inMemoryToken = null
        inMemoryUserId = null
        prefs?.edit()?.clear()?.apply()
    }
}
