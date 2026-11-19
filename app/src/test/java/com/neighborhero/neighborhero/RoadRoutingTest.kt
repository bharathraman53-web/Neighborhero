package com.neighborhero.neighborhero

import com.neighborhero.neighborhero.data.network.RoadRouteResult
import com.neighborhero.neighborhero.data.network.RoadRoutingService
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.osmdroid.util.GeoPoint

/**
 * Unit tests for Road-Based Routing integration (OSRM OpenStreetMap),
 * route geometry parsing, distance/ETA formatting, and client-side recalculation throttling.
 */
class RoadRoutingTest {

    @Test
    fun testHaversineDistanceCalculation() {
        // Distance between Chennai Central (13.0827, 80.2707) and nearby point (13.0910, 80.2760)
        val dMeters = RoadRoutingService.distanceBetweenMeters(13.0827, 80.2707, 13.0910, 80.2760)
        // Expected straight-line distance is ~1090 meters
        assertTrue("Distance should be approximately 1090 meters", dMeters in 1000.0..1200.0)

        // Same point distance should be 0
        val zeroDist = RoadRoutingService.distanceBetweenMeters(13.0827, 80.2707, 13.0827, 80.2707)
        assertEquals(0.0, zeroDist, 0.001)
    }

    @Test
    fun testRoadRouteResultMetrics() {
        val origin = GeoPoint(13.0910, 80.2760)
        val destination = GeoPoint(13.0827, 80.2707)
        val points = listOf(origin, GeoPoint(13.0870, 80.2730), destination)

        // 2400 meters (~1.49 miles), 180 seconds (3 min)
        val result = RoadRouteResult(
            points = points,
            distanceMeters = 2400.0,
            durationSeconds = 180.0,
            isFromCache = false,
            origin = origin,
            destination = destination
        )

        assertEquals(2.4, result.distanceKm, 0.01)
        assertEquals(1.49, result.distanceMiles, 0.02)
        assertEquals(3, result.etaMinutes)
        assertEquals("1.5 mi", result.formattedDistance(useMiles = true))
        assertEquals("2.4 km", result.formattedDistance(useMiles = false))
        assertEquals("~3 min", result.formattedEta())
    }

    @Test
    fun testEtaMinutesEdgeCases() {
        val origin = GeoPoint(13.0828, 80.2708)
        val destination = GeoPoint(13.0827, 80.2707)

        // Arrived at destination (<= 35m)
        val arrivedResult = RoadRouteResult(
            points = listOf(origin, destination),
            distanceMeters = 25.0,
            durationSeconds = 10.0,
            origin = origin,
            destination = destination
        )
        assertEquals(0, arrivedResult.etaMinutes)
        assertEquals("Arriving now", arrivedResult.formattedEta())

        // 1 minute arrival
        val oneMinResult = RoadRouteResult(
            points = listOf(origin, destination),
            distanceMeters = 200.0,
            durationSeconds = 45.0,
            origin = origin,
            destination = destination
        )
        assertEquals(1, oneMinResult.etaMinutes)
        assertEquals("~1 min", oneMinResult.formattedEta())
    }

    @Test
    fun testOsrmGeoJsonParsing() {
        // Simulated OSRM response JSON format with road-following geometry
        val jsonString = """
            {
                "code": "Ok",
                "routes": [
                    {
                        "distance": 2414.5,
                        "duration": 163.2,
                        "geometry": {
                            "coordinates": [
                                [80.276000, 13.091000],
                                [80.275450, 13.089800],
                                [80.273200, 13.086400],
                                [80.270700, 13.082700]
                            ],
                            "type": "LineString"
                        }
                    }
                ]
            }
        """.trimIndent()

        val jsonElement = com.google.gson.JsonParser.parseString(jsonString).asJsonObject
        assertEquals("Ok", jsonElement.get("code").asString)

        val routes = jsonElement.getAsJsonArray("routes")
        val routeObj = routes.get(0).asJsonObject
        val distance = routeObj.get("distance").asDouble
        val duration = routeObj.get("duration").asDouble
        val coords = routeObj.getAsJsonObject("geometry").getAsJsonArray("coordinates")

        val points = mutableListOf<GeoPoint>()
        for (i in 0 until coords.size()) {
            val pair = coords.get(i).asJsonArray
            // GeoJSON provides [longitude, latitude]
            val lng = pair.get(0).asDouble
            val lat = pair.get(1).asDouble
            points.add(GeoPoint(lat, lng))
        }

        assertEquals(4, points.size)
        // Verify coordinate conversion to GeoPoint(lat, lon)
        assertEquals(13.091000, points[0].latitude, 0.00001)
        assertEquals(80.276000, points[0].longitude, 0.00001)
        assertEquals(13.082700, points[3].latitude, 0.00001)
        assertEquals(80.270700, points[3].longitude, 0.00001)

        val parsedRoute = RoadRouteResult(
            points = points,
            distanceMeters = distance,
            durationSeconds = duration,
            origin = points.first(),
            destination = points.last()
        )

        assertEquals(2.41, parsedRoute.distanceKm, 0.01)
        assertEquals(1.5, parsedRoute.distanceMiles, 0.05)
        assertEquals(3, parsedRoute.etaMinutes)
        assertEquals("~3 min", parsedRoute.formattedEta())
    }

    @Test
    fun testThrottlingThresholdConstants() {
        // Validate minimum movement threshold (25m - 30m)
        assertTrue(
            "Recalculation distance threshold should be between 20m and 35m",
            RoadRoutingService.MIN_RECALCULATION_DISTANCE_METERS in 20.0..35.0
        )

        // Validate cooldown interval (>= 5 seconds)
        assertTrue(
            "Recalculation cooldown interval should be at least 5000ms",
            RoadRoutingService.MIN_RECALCULATION_INTERVAL_MS >= 5000L
        )
    }

    @Test
    fun testTrimRemainingRoadRouteProgress() {
        // Simulating a route with 4 points along a road
        val p0 = GeoPoint(13.0820, 80.2700)
        val p1 = GeoPoint(13.0840, 80.2720)
        val p2 = GeoPoint(13.0860, 80.2740)
        val p3 = GeoPoint(13.0880, 80.2760) // Destination
        val fullRoute = listOf(p0, p1, p2, p3)

        // Helper has moved forward to a new location near p1
        val helperNearP1 = GeoPoint(13.0841, 80.2721)
        val trimmed = RoadRoutingService.trimRemainingRoadRoute(helperNearP1, fullRoute)

        // 1. Must start with helper's exact current location
        assertEquals(helperNearP1.latitude, trimmed.first().latitude, 0.000001)
        assertEquals(helperNearP1.longitude, trimmed.first().longitude, 0.000001)

        // 2. Already travelled p0 should be removed
        assertFalse("Already travelled point p0 should be trimmed away", trimmed.contains(p0))

        // 3. Destination p3 must be the final point
        assertEquals(p3.latitude, trimmed.last().latitude, 0.000001)
        assertEquals(p3.longitude, trimmed.last().longitude, 0.000001)

        // 4. Remaining geometry p2 and p3 must be preserved
        assertTrue("p2 must be in remaining route", trimmed.contains(p2))
        assertTrue("p3 must be in remaining route", trimmed.contains(p3))
    }

    @Test
    fun testTrimRemainingRoadRouteAtDestination() {
        val p0 = GeoPoint(13.0820, 80.2700)
        val p1 = GeoPoint(13.0840, 80.2720)
        val pDest = GeoPoint(13.0880, 80.2760)
        val fullRoute = listOf(p0, p1, pDest)

        // Helper has arrived at destination
        val helperAtDest = GeoPoint(13.08801, 80.27601)
        val trimmed = RoadRoutingService.trimRemainingRoadRoute(helperAtDest, fullRoute)

        assertEquals(2, trimmed.size)
        assertEquals(helperAtDest.latitude, trimmed.first().latitude, 0.000001)
        assertEquals(pDest.latitude, trimmed.last().latitude, 0.000001)
    }

    @Test
    fun testIsDeviatedFromRoute() {
        val p0 = GeoPoint(13.0820, 80.2700)
        val p1 = GeoPoint(13.0840, 80.2720)
        val route = listOf(p0, p1)

        // Point directly on/very close to route (~5 meters away)
        val onRoutePoint = GeoPoint(13.08202, 80.27002)
        assertFalse("Point close to route should not be considered deviated",
            RoadRoutingService.isDeviatedFromRoute(onRoutePoint, route, thresholdMeters = 40.0)
        )

        // Point far off route (e.g. 500 meters away)
        val offRoutePoint = GeoPoint(13.0900, 80.2800)
        assertTrue("Point 500m away should be detected as deviated",
            RoadRoutingService.isDeviatedFromRoute(offRoutePoint, route, thresholdMeters = 40.0)
        )
    }

    @Test
    fun testApproximateDistanceInKilometres() {
        // Distance between 13.0827, 80.2707 and 13.0910, 80.2760
        val dMeters = RoadRoutingService.distanceBetweenMeters(13.0827, 80.2707, 13.0910, 80.2760)
        val distKm = dMeters / 1000.0

        // Verify distance is in km (expected ~1.09 km)
        assertTrue("Distance in km should be ~1.09 km", distKm in 1.0..1.2)
        val formatted = String.format(java.util.Locale.US, "Approx. %.1f km away", distKm)
        assertEquals("Approx. 1.1 km away", formatted)
    }
}
