package com.neighborhero.neighborhero.data.network

import android.os.Build

/**
 * Centralized API configuration for NeighborHero.
 *
 * Addressing Modes:
 * - Android Emulator: "http://10.0.2.2/neighborhero-api/public/api/" (routes to host PC)
 * - Physical Phone via Wi-Fi LAN: "http://172.23.48.169/neighborhero-api/public/api/"
 * - Physical Phone via USB (ADB reverse fallback): "http://127.0.0.1/neighborhero-api/public/api/"
 * - Production: "https://api.neighborhero.org/api/"
 */
object ApiConfig {
    /**
     * Development PC Wi-Fi LAN IP address for direct physical phone connection.
     * Update this IP whenever your development PC's Wi-Fi IP address changes.
     */
    const val DEV_PC_LAN_IP = "172.23.48.169"
    const val DEV_API_PORT = 80
    const val DEV_API_PATH = "neighborhero-api/public/api/"

    /**
     * Default LAN URL for physical Android devices connected on the same Wi-Fi.
     * Does NOT require ADB reverse port forwarding.
     */
    const val DEFAULT_PHYSICAL_DEVICE_LAN_URL = "http://$DEV_PC_LAN_IP/neighborhero-api/public/api/"

    /**
     * Default URL for the Android Emulator (10.0.2.2 routes directly to the host PC loopback).
     */
    const val DEFAULT_EMULATOR_URL = "http://10.0.2.2/neighborhero-api/public/api/"

    /**
     * Fallback URL for physical devices when connected via USB with ADB reverse forwarding.
     */
    const val DEFAULT_USB_DEVICE_URL = "http://127.0.0.1/neighborhero-api/public/api/"

    const val DEFAULT_XAMPP_APACHE_URL = DEFAULT_EMULATOR_URL

    private var dynamicBaseUrl: String? = null

    /**
     * Determines whether the current device is an Android emulator.
     */
    fun isEmulator(): Boolean {
        return try {
            val fingerprint = Build.FINGERPRINT ?: ""
            val model = Build.MODEL ?: ""
            val manufacturer = Build.MANUFACTURER ?: ""
            val brand = Build.BRAND ?: ""
            val device = Build.DEVICE ?: ""
            val product = Build.PRODUCT ?: ""
            val hardware = Build.HARDWARE ?: ""

            fingerprint.startsWith("generic") ||
                fingerprint.startsWith("unknown") ||
                model.contains("google_sdk", ignoreCase = true) ||
                model.contains("Emulator", ignoreCase = true) ||
                model.contains("Android SDK built for", ignoreCase = true) ||
                manufacturer.contains("Genymotion", ignoreCase = true) ||
                (brand.startsWith("generic") && device.startsWith("generic")) ||
                product.contains("sdk_gphone", ignoreCase = true) ||
                product.contains("google_sdk", ignoreCase = true) ||
                hardware.contains("goldfish", ignoreCase = true) ||
                hardware.contains("ranchu", ignoreCase = true)
        } catch (_: Throwable) {
            false
        }
    }

    /**
     * Automatically resolves the default base URL:
     * - On Android Emulator: DEFAULT_EMULATOR_URL (http://10.0.2.2/neighborhero-api/public/api/)
     * - On Physical Phone: DEFAULT_PHYSICAL_DEVICE_LAN_URL (http://172.23.48.169/neighborhero-api/public/api/)
     */
    val defaultUrl: String
        get() = if (isEmulator()) DEFAULT_EMULATOR_URL else DEFAULT_PHYSICAL_DEVICE_LAN_URL

    var baseUrl: String
        get() = dynamicBaseUrl ?: defaultUrl
        set(value) {
            dynamicBaseUrl = if (value.endsWith("/")) value else "$value/"
            NetworkClient.resetClient()
        }

    fun resetToDefault() {
        dynamicBaseUrl = null
        NetworkClient.resetClient()
    }

    fun usePhysicalPhoneLan(ipAddress: String = DEV_PC_LAN_IP, port: Int = DEV_API_PORT) {
        baseUrl = if (port == 80) {
            "http://$ipAddress/$DEV_API_PATH"
        } else {
            "http://$ipAddress:$port/$DEV_API_PATH"
        }
    }

    fun useUsbPhysicalPhone() {
        baseUrl = DEFAULT_USB_DEVICE_URL
    }

    fun useEmulator() {
        baseUrl = DEFAULT_EMULATOR_URL
    }

    fun useXamppApache() {
        baseUrl = DEFAULT_XAMPP_APACHE_URL
    }

    fun setLanAddress(ipAddress: String, port: Int = DEV_API_PORT) {
        usePhysicalPhoneLan(ipAddress, port)
    }
}
