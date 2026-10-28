package com.neighborhero.neighborhero.data.network

/**
 * Centralized API configuration for NeighborHero.
 *
 * Addressing Modes:
 * - Android Emulator: commonly "http://10.0.2.2:8000/api/" (PHP built-in server)
 *   or "http://10.0.2.2/neighborhero-api/public/api/" (XAMPP Apache virtual host)
 * - Physical Phone on LAN: "http://<YOUR_LAN_IP>:8000/api/"
 * - Production: "https://api.neighborhero.org/api/"
 */
object ApiConfig {
    const val DEFAULT_EMULATOR_URL = "http://10.0.2.2:8000/api/"
    const val DEFAULT_XAMPP_APACHE_URL = "http://10.0.2.2/neighborhero-api/public/api/"

    private var dynamicBaseUrl: String? = null

    var baseUrl: String
        get() = dynamicBaseUrl ?: DEFAULT_XAMPP_APACHE_URL
        set(value) {
            dynamicBaseUrl = if (value.endsWith("/")) value else "$value/"
            NetworkClient.resetClient()
        }

    fun setLanAddress(ipAddress: String, port: Int = 8000) {
        baseUrl = "http://$ipAddress:$port/api/"
    }

    fun useEmulator() {
        baseUrl = DEFAULT_EMULATOR_URL
    }

    fun useXamppApache() {
        baseUrl = DEFAULT_XAMPP_APACHE_URL
    }
}
