package com.inzhood.core.library.gps.model

/*
* This is used to determine how frequently to get the new point. If traveling fast, less frequently
* Update is based on speed.
* */
const val HALF_MINUTE_UPDATE = 30000L

class TransportSpeeds {
    companion object {
        private val speeds: List<TransportSpeed> = listOf(
            TransportSpeed("Walking", 5.0, 1.39, 2000L), // 2 seconds is 2 1/2 meter
            TransportSpeed("Bicycle", 20.0, 5.56, 3000L), // 3 seconds is 15 meters
            TransportSpeed("Scooter", 40.0, 11.11, 5000L), // 5 seconds is 50 meters
            TransportSpeed("Automobile", 80.0, 22.22, 5000L) // 5 seconds is 1/10 km
            ,TransportSpeed("Helicopter", 240.0, 66.67, 5000L) // 5 seconds is 1/3 kilometer
        )

        fun getLocationUpdatesForMode(mode: String): Long {
            val transportSpeed = speeds.find { it.modeKey == mode }
            return transportSpeed?.updateFrequencyMs ?: 20000L

        }

        // for updating the route array get the max speed of a particular type of transport
        fun setUpdateFrequencyBySpeedsPosition(position: Int): Long {
            return if (position in speeds.indices) {
                val transportSpeed = speeds[position]
                transportSpeed.updateFrequencyMs
            } else {
                HALF_MINUTE_UPDATE
            }
        }
    }
}

data class TransportSpeed(
    val modeKey: String, // actual string in strings.xml
    val speedKph: Double,
    val speedMps: Double,
    val updateFrequencyMs: Long
)


