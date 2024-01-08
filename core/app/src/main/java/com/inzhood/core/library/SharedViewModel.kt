package com.inzhood.core.library

import android.content.Context
import android.location.Location
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import com.inzhood.core.library.gps.awaitLastLocation
import com.inzhood.core.library.gps.flowTrackLocation
import com.inzhood.core.library.gps.model.TransportSpeeds
import com.inzhood.core.library.gps.trueTransportSpeedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.launch

class SharedViewModel  @ViewModelInject constructor(context: Context) : ViewModel() {
    annotation class ViewModelInject

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    private val _location = MutableLiveData<Location>()
    val location: LiveData<Location> = _location


    fun trackLocation() {
        viewModelScope.launch { // Use viewModelScope for lifecycle-aware coroutines
            fusedLocationClient.flowTrackLocation()
                .conflate()
                .catch { e ->
                    Log.e("GpsLocationViewModel", "Unable to get location", e)
                    // TODO: notifying the UI about the error
                }
                .collect { location ->
                    _location.value = location
                }
        }
    }

    fun lastLocation() {
        viewModelScope.launch {
            try {
                val lastLocation = fusedLocationClient.awaitLastLocation()
                _location.value = lastLocation // Update LiveData with last known location
            } catch (e: Exception) {
                Log.e("GpsLocationViewModel", "Unable to get last known location", e)
                // TODO:  Consider notifying the UI about the error
            }
        }
    }

    fun doUpdateRateFrequency(selectedItemPosition: Int) {
        trueTransportSpeedFlow.value = TransportSpeeds.setUpdateFrequencyBySpeedsPosition(selectedItemPosition)
    }
}

// *************************************   FACTORY  ********************************************
class GpsLocationViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SharedViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SharedViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}