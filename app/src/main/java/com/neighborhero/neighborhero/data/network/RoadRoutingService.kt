package com.neighborhero.neighborhero.data.network

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import org.osmdroid.util.GeoPoint
import java.io.IOException
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Result of a road-following route calculation between two geographic coordinates.
 */
data class RoadRouteResult(
    val points: List<GeoPoint>,
    val distanceMeters: Double,
    val durationSeconds: Double,
    val isFromCache: Boolean = false,
    val origin: GeoPoint,
    val destination: GeoPoint,
    val fetchedAtMs: Long = System.currentTimeMillis()
) {
    /**
     * Road distance in kilometers rounded to two decimals.
     */
    val distanceKm: Double
        get() = Math.round((distanceMeters / 1000.0) * 100.0) / 100.0

    /**
     * Road distance in miles rounded to two decimals.
     */
    val distanceMiles: Double
        get() = Math.round((distanceMeters / 1609.344) * 100.0) / 100.0

    /**
     * Road-based Estimated Time of Arrival (ETA) in minutes.
     * Returns 0 if within 35 meters (arrived/at destination), otherwise at least 1 minute.
     */
    val etaMinutes: Int
        get() = when {
            distanceMeters <= 35.0 -> 0
            else -> maxOf(1, Math.round(durationSeconds / 60.0).toInt())
        }

    fun formattedDistance(useMiles: Boolean = true): String {
        return if (useMiles) {
            String.format(Locale.US, "%.1f mi", distanceMiles)
        } else {
            String.format(Locale.US, "%.1f km", distanceKm)
        }
    }

    fun formattedEta(): String {
        return when (etaMinutes) {
            0 -> "Arriving now"
            1 -> "~1 min"
            else -> "~$etaMinutes min"
        }
    }
}

/**
 * Road Routing Service integrating OpenStreetMap (OSRM) driving route geometry.
 *
 * Features:
 * - Returns road-following line geometry conforming to real street networks.
 * - Free open-source OpenStreetMap routing without paid API keys.
 * - Dual-mirror architecture (Primary: project-osrm.org, Fallback: routing.openstreetmap.de).
 * - Intelligent client-side throttling (25-meter movement threshold + 5s cooldown).
 * - Graceful degradation on network errors (retains last valid road geometry).
 * - Strict task privacy: Never sends authorization tokens to external routing servers.
 */
class RoadRoutingService private constructor() {

    companion object {
        private const val TAG = "RoadRoutingService"
        private const val USER_AGENT = "NeighborHero-Android-App/1.0"

        // Primary and mirror OpenStreetMap OSRM routing endpoints
        private const val PRIMARY_OSRM_URL =
            "https://router.project-osrm.org/route/v1/driving/%.6f,%.6f;%.6f,%.6f?overview=full&geometries=geojson"
        private const val FALLBACK_OSRM_URL =
            "https://routing.openstreetmap.de/routed-car/route/v1/driving/%.6f,%.6f;%.6f,%.6f?overview=full&geometries=geojson"

        // Minimum distance in meters the helper must move before requesting route recalculation
        const val MIN_RECALCULATION_DISTANCE_METERS = 25.0

        // Minimum interval between network route requests to prevent server spamming
        const val MIN_RECALCULATION_INTERVAL_MS = 5000L

        @Volatile
        private var instance: RoadRoutingService? = null

        fun getInstance(): RoadRoutingService {
            return instance ?: synchronized(this) {
                instance ?: RoadRoutingService().also { instance = it }
            }
        }

        /**
         * Computes Great Circle (Haversine) surface distance in meters between two lat/lon pairs.
         */
        fun distanceBetweenMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
            val earthRadius = 6371000.0 // meters
            val dLat = Math.toRadians(lat2 - lat1)
            val dLon = Math.toRadians(lon2 - lon1)
            val a = sin(dLat / 2) * sin(dLat / 2) +
                    cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                    sin(dLon / 2) * sin(dLon / 2)
            val c = 2 * atan2(sqrt(a), sqrt(1 - a))
            return earthRadius * c
        }

        /**
         * Checks whether the helper has deviated from the current road route.
         * Returns true if the helper's distance to the closest route point exceeds [thresholdMeters].
         */
        fun isDeviatedFromRoute(helperPos: GeoPoint, routePoints: List<GeoPoint>, thresholdMeters: Double = 40.0): Boolean {
            if (routePoints.isEmpty()) return true
            var minDistance = Double.MAX_VALUE
            for (pt in routePoints) {
                val dist = distanceBetweenMeters(
                    helperPos.latitude, helperPos.longitude,
                    pt.latitude, pt.longitude
                )
                if (dist < minDistance) {
                    minDistance = dist
                }
            }
            return minDistance > thresholdMeters
        }

        /**
         * Trims the already-travelled portion of a road route and starts the remaining
         * route geometry from the helper's latest GPS position.
         *
         * @param helperPos The helper's latest GPS coordinates.
         * @param routePoints Full list of coordinates of the road route to the destination.
         * @return Remaining road route points starting at [helperPos] and leading to the destination.
         */
        fun trimRemainingRoadRoute(helperPos: GeoPoint, routePoints: List<GeoPoint>): List<GeoPoint> {
            if (routePoints.isEmpty()) return emptyList()
            if (routePoints.size <= 2) {
                return listOf(helperPos, routePoints.last())
            }

            // Find the point on the route closest to the helper's current position
            var minDistance = Double.MAX_VALUE
            var closestIndex = 0

            for (i in routePoints.indices) {
                val dist = distanceBetweenMeters(
                    helperPos.latitude, helperPos.longitude,
                    routePoints[i].latitude, routePoints[i].longitude
                )
                if (dist < minDistance) {
                    minDistance = dist
                    closestIndex = i
                }
            }

            val destination = routePoints.last()

            // If helper has reached or is closest to the destination
            if (closestIndex >= routePoints.size - 1) {
                return listOf(helperPos, destination)
            }

            // Slice away all points prior to the helper's progress
            val nextIndex = closestIndex + 1
            val remainingSublist = routePoints.subList(nextIndex, routePoints.size)

            val remainingPoints = ArrayList<GeoPoint>(remainingSublist.size + 1)
            remainingPoints.add(helperPos)
            remainingPoints.addAll(remainingSublist)
            return remainingPoints
        }
    }

    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(7, TimeUnit.SECONDS)
        .readTimeout(7, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    private val cacheLock = Any()
    private var lastResult: RoadRouteResult? = null
    private var lastRequestTimeMs: Long = 0L

    /**
     * Determines whether updated GPS coordinates necessitate requesting a new road route.
     * Prevents unnecessary API calls when helper is stationary or moving less than [MIN_RECALCULATION_DISTANCE_METERS].
     */
    fun shouldRecalculate(currentOrigin: GeoPoint, currentDest: GeoPoint): Boolean {
        synchronized(cacheLock) {
            val cached = lastResult ?: return true

            val timeSinceLast = System.currentTimeMillis() - lastRequestTimeMs
            if (timeSinceLast < MIN_RECALCULATION_INTERVAL_MS) {
                return false
            }

            val originShift = distanceBetweenMeters(
                cached.origin.latitude, cached.origin.longitude,
                currentOrigin.latitude, currentOrigin.longitude
            )

            val destShift = distanceBetweenMeters(
                cached.destination.latitude, cached.destination.longitude,
                currentDest.latitude, currentDest.longitude
            )

            val isDeviated = isDeviatedFromRoute(currentOrigin, cached.points)

            // Recalculate if helper moved >= 25m, destination shifted >= 30m, or helper deviated from route
            return originShift >= MIN_RECALCULATION_DISTANCE_METERS || destShift >= 30.0 || isDeviated
        }
    }

    /**
     * Retrieves road route geometry, road distance, and travel time.
     *
     * @param originLat Helper current latitude.
     * @param originLng Helper current longitude.
     * @param destLat Requester destination latitude.
     * @param destLng Requester destination longitude.
     * @param forceRefresh Set to true to bypass client-side distance and time throttling.
     */
    suspend fun fetchRoadRoute(
        originLat: Double,
        originLng: Double,
        destLat: Double,
        destLng: Double,
        forceRefresh: Boolean = false
    ): Result<RoadRouteResult> = withContext(Dispatchers.IO) {
        val origin = GeoPoint(originLat, originLng)
        val destination = GeoPoint(destLat, destLng)

        // Check if cached route is fresh and helper hasn't moved significantly
        synchronized(cacheLock) {
            if (!forceRefresh && !shouldRecalculate(origin, destination) && lastResult != null) {
                return@withContext Result.success(lastResult!!.copy(isFromCache = true))
            }
        }

        // Try Primary OSRM URL first, then Fallback mirror
        val endpoints = listOf(PRIMARY_OSRM_URL, FALLBACK_OSRM_URL)
        var lastException: Exception? = null

        for (template in endpoints) {
            try {
                // OSRM requires coordinates in longitude,latitude order
                val url = String.format(Locale.US, template, originLng, originLat, destLng, destLat)
                val request = Request.Builder()
                    .url(url)
                    .header("User-Agent", USER_AGENT)
                    .header("Accept", "application/json")
                    .get()
                    .build()

                httpClient.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        throw IOException("HTTP ${response.code} from routing service")
                    }

                    val bodyString = response.body?.string()
                        ?: throw IOException("Empty response body from routing service")

                    val json = JSONObject(bodyString)
                    val code = json.optString("code")
                    if (code != "Ok") {
                        throw IOException("OSRM returned status code: $code")
                    }

                    val routesArray = json.optJSONArray("routes")
                    if (routesArray == null || routesArray.length() == 0) {
                        throw IOException("No road routes returned for given coordinates")
                    }

                    val firstRoute = routesArray.getJSONObject(0)
                    val distanceMeters = firstRoute.optDouble("distance", 0.0)
                    val durationSeconds = firstRoute.optDouble("duration", 0.0)

                    val geometry = firstRoute.optJSONObject("geometry")
                        ?: throw IOException("Missing geometry object in route")

                    val coordinates = geometry.optJSONArray("coordinates")
                        ?: throw IOException("Missing coordinates array in route geometry")

                    val points = ArrayList<GeoPoint>(coordinates.length())
                    for (i in 0 until coordinates.length()) {
                        val pair = coordinates.getJSONArray(i)
                        val lng = pair.getDouble(0)
                        val lat = pair.getDouble(1)
                        points.add(GeoPoint(lat, lng))
                    }

                    if (points.isEmpty()) {
                        throw IOException("Parsed 0 road route points")
                    }

                    val routeResult = RoadRouteResult(
                        points = points,
                        distanceMeters = distanceMeters,
                        durationSeconds = durationSeconds,
                        isFromCache = false,
                        origin = origin,
                        destination = destination
                    )

                    synchronized(cacheLock) {
                        lastResult = routeResult
                        lastRequestTimeMs = System.currentTimeMillis()
                    }

                    Log.i(TAG, "Road route fetched successfully: ${points.size} points, ${routeResult.formattedDistance()}, ${routeResult.formattedEta()}")
                    return@withContext Result.success(routeResult)
                }
            } catch (e: Exception) {
                lastException = e
                Log.w(TAG, "Routing attempt via $template failed: ${e.message}")
            }
        }

        // On complete failure, gracefully return cached route if available
        synchronized(cacheLock) {
            val cached = lastResult
            if (cached != null) {
                Log.i(TAG, "Returning last known valid road route after network failure")
                return@withContext Result.success(cached.copy(isFromCache = true))
            }
        }

        Result.failure(lastException ?: IOException("Failed to fetch road route from all routing endpoints"))
    }

    /**
     * Clears cached routing geometry and timestamps (e.g. when tracking session completes).
     */
    fun clearCache() {
        synchronized(cacheLock) {
            lastResult = null
            lastRequestTimeMs = 0L
        }
    }
}
