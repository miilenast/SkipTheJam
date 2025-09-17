package com.example.skipthejam.viewmodel

import com.example.skipthejam.service.LocationService
import android.app.Application
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LocationViewModel(application: Application) : AndroidViewModel(application) {
    private val locationService = LocationService(application.applicationContext)
    private val _location = MutableStateFlow<Location?>(null)
    val location: StateFlow<Location?> = _location.asStateFlow()

    init{
        startTracking()
    }

    fun startTracking() {
        locationService.onLocation = { loc ->
            _location.value = loc
        }
        locationService.startLocationUpdates()
    }

    fun stopTracking() {
        locationService.stopLocationUpdates()
    }

    override fun onCleared() {
        stopTracking()
        super.onCleared()
    }

    fun getCurrentLocation(): Location? {
        return locationService.getCurrentLocation()
    }
}