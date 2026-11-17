package com.neighborhero.neighborhero.data.network

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context? = null) {
    private val prefs: SharedPreferences? = context?.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "neighborhero_session"
        private const val KEY_TOKEN = "auth_bearer_token"
        private const val KEY_USER_ID = "authenticated_user_id"
        private const val KEY_ROLE = "authenticated_user_role"
        private const val KEY_LOCATION = "selected_neighborhood_location"
        private const val KEY_LATITUDE = "selected_location_lat"
        private const val KEY_LONGITUDE = "selected_location_lng"

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
    private var inMemoryRole: String? = null
    private var inMemoryLocation: String? = null
    private var inMemoryLat: Double? = null
    private var inMemoryLng: Double? = null

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

    fun saveRole(role: String?) {
        inMemoryRole = role
        prefs?.edit()?.putString(KEY_ROLE, role)?.apply()
    }

    fun getRole(): String? {
        return prefs?.getString(KEY_ROLE, inMemoryRole) ?: inMemoryRole
    }

    fun saveLocation(location: String?, lat: Double? = null, lng: Double? = null) {
        inMemoryLocation = location
        inMemoryLat = lat
        inMemoryLng = lng
        val editor = prefs?.edit()
        if (editor != null) {
            if (location != null) editor.putString(KEY_LOCATION, location) else editor.remove(KEY_LOCATION)
            if (lat != null) editor.putFloat(KEY_LATITUDE, lat.toFloat()) else editor.remove(KEY_LATITUDE)
            if (lng != null) editor.putFloat(KEY_LONGITUDE, lng.toFloat()) else editor.remove(KEY_LONGITUDE)
            editor.apply()
        }
    }

    fun getSelectedLocation(): String? {
        val stored = prefs?.getString(KEY_LOCATION, null)
        return stored ?: inMemoryLocation
    }

    fun getCoordinates(): Pair<Double, Double>? {
        if (prefs != null && prefs.contains(KEY_LATITUDE) && prefs.contains(KEY_LONGITUDE)) {
            val lat = prefs.getFloat(KEY_LATITUDE, 0f).toDouble()
            val lng = prefs.getFloat(KEY_LONGITUDE, 0f).toDouble()
            return Pair(lat, lng)
        }
        if (inMemoryLat != null && inMemoryLng != null) {
            return Pair(inMemoryLat!!, inMemoryLng!!)
        }
        return null
    }

    fun isLoggedIn(): Boolean {
        val t = getToken()
        return !t.isNullOrBlank()
    }

    fun clearSession() {
        inMemoryToken = null
        inMemoryUserId = null
        inMemoryRole = null
        inMemoryLocation = null
        inMemoryLat = null
        inMemoryLng = null
        prefs?.edit()?.clear()?.apply()
    }
}
