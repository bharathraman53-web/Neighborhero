package com.neighborhero.neighborhero.data.network

import android.os.Build

/**
 * Centralized API configuration for NeighborHero.
 *
 * Addressing Modes:
 * - Android Emulator: "http://10.0.2.2/neighborhero-api/public/api/" (routes to host PC)
 * - Physical Phone via USB (ADB reverse): "http://127.0.0.1/neighborhero-api/public/api/"
 * - Physical Phone via Wi-Fi LAN: "http://<PC_LAN_IP>/neighborhero-api/public/api/"
 * - Production: "https://api.neighborhero.org/api/"
 */
object ApiConfig {
    const val DEFAULT_EMULATOR_URL = "http://10.0.2.2/neighborhero-api/public/api/"
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
     * - On Android Emulator: DEFAULT_EMULATOR_URL (10.0.2.2)
     * - On Physical Phone (via ADB reverse over USB): DEFAULT_USB_DEVICE_URL (127.0.0.1)
     */
    val defaultUrl: String
        get() = if (isEmulator()) DEFAULT_EMULATOR_URL else DEFAULT_USB_DEVICE_URL

    var baseUrl: String
        get() = dynamicBaseUrl ?: defaultUrl
        set(value) {
            dynamicBaseUrl = if (value.endsWith("/")) value else "$value/"
            NetworkClient.resetClient()
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

    fun setLanAddress(ipAddress: String, port: Int = 80) {
        baseUrl = "http://$ipAddress:$port/neighborhero-api/public/api/"
    }
}
