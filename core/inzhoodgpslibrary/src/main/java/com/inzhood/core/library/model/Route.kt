package com.inzhood.core.library.model

import android.location.Location
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.time.Instant
import kotlinx.serialization.serializer

class Route(
    private val locations: MutableList<Location> = ArrayList()
) : MutableList<Location> by locations {
    @Serializable
    data class LocationData(val x: Double, val y: Double, val timestamp: String)
    fun calculateDistanceBetween(index1: Int, index2: Int): Double {
        val location1 = locations[index1]
        val location2 = locations[index2]

        // Implement distance calculation logic here
        val distance = calculateDistance(location1, location2)

        return distance
    }

    private fun calculateDistance(location1: Location, location2: Location): Double {
        // Replace this with your preferred distance calculation formula,
        // such as the Haversine formula for spherical Earth distances:
        val earthRadius = 6371.0  // Earth radius in kilometers

        val lat1 = Math.toRadians(location1.latitude)
        val lon1 = Math.toRadians(location1.longitude)
        val lat2 = Math.toRadians(location2.latitude)
        val lon2 = Math.toRadians(location2.longitude)

        val dLat = lat2 - lat1
        val dLon = lon2 - lon1

        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(lat1) * Math.cos(lat2) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)

        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

        val distance = earthRadius * c

        return distance
    }
    fun writeAsJson(filename: String) {
        val locationData = locations.map { location ->
            LocationData(
                x = location.longitude,
                y = location.latitude,
                timestamp = Instant.now().toString()
            )
        }

        val jsonString = Json.encodeToString(locationData)

        File(filename).writeText(jsonString)
    }
    // Extension function
    // how to use:  val distance = route.calculateDistanceBetween(0, 2)  // Calculate distance between first and third locations
    fun Location.timeFromPreviousLocation(previousLocation: Location): Long {
        return this.time - previousLocation.time
    }
}
