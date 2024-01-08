package com.inzhood.core.library.gps

import android.annotation.SuppressLint
import android.location.Location
import android.os.Looper
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.inzhood.core.library.gps.model.TransportSpeeds
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

val trueTransportSpeedFlow = MutableStateFlow(TransportSpeeds.getLocationUpdatesForMode("Walking"))

private fun createLocationRequest(): LocationRequest = LocationRequest.Builder(
    Priority.PRIORITY_HIGH_ACCURACY,
    trueTransportSpeedFlow.value
).setIntervalMillis(trueTransportSpeedFlow.value).build()

fun Location.asString(format: Int = Location.FORMAT_DEGREES): String {
    val latitude = Location.convert(latitude, format)
    val longitude = Location.convert(longitude, format)
    return "Location is: $latitude, $longitude"
}

@SuppressLint("MissingPermission")
suspend fun FusedLocationProviderClient.awaitLastLocation(): Location =
    suspendCancellableCoroutine { continuation ->
        lastLocation.addOnSuccessListener { location ->
            continuation.resume(location)
        }.addOnFailureListener { e ->
            continuation.resumeWithException(e)
        }
    }

@SuppressLint("MissingPermission")
fun FusedLocationProviderClient.flowTrackLocation(): Flow<Location> = callbackFlow {
    val callback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            for (location in result.locations) {
                trySend(location)
                    .isSuccess
                    .also { success ->
                        if (!success) {
                            //TODO Handle the case where sending failed, potentially due to a closed channel
                            Log.e("LocationUtils", "sending failed, is channel closed?")
                        }
                    }
            }
        }
    }
    // Remove any existing location updates before starting new ones
    removeLocationUpdates(callback) // Add this line
    // SHIMON: This code is called within THIS fun FusedLocationProviderClient.flowTrackerLocation
    requestLocationUpdates(createLocationRequest(), callback, Looper.getMainLooper())
        .addOnFailureListener { e -> close(e) }
    awaitClose {
        removeLocationUpdates(callback)
    }
}