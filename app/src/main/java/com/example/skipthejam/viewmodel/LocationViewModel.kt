package com.example.skipthejam.viewmodel

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import com.google.android.gms.location.*
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class LocationViewModel(application: Application) : AndroidViewModel(application) {
    private val context = application.applicationContext
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    private val _location = MutableStateFlow<Location?>(null)
    val location: StateFlow<Location?> = _location.asStateFlow()

    private var locationCallback: LocationCallback? = null
    private var sendJob: Job? = null

    init{
        startTracking()
    }

    fun startTracking() {
        if (locationCallback != null) return

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            return

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            5000L
        )
            .setMinUpdateIntervalMillis(2000L)
            .setWaitForAccurateLocation(true)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let {
                    _location.value = it
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback!!,
            Looper.getMainLooper()
        )

        sendLocations()
    }

    fun sendLocations(){
        if(sendJob != null) return

        sendJob = viewModelScope.launch {
            while(isActive) {
                val currentLocation = _location.value
                val uid = auth.currentUser?.uid
                if(currentLocation != null && uid != null) {
                    db.collection("users").document(uid)
                        .update(
                            mapOf(
                                "latitude" to currentLocation.latitude,
                                "longitude" to currentLocation.longitude
                            )
                        )
                }
                delay(5000L)
            }
        }
    }

    fun stopTracking() {
        locationCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
        }
        locationCallback = null
    }

    override fun onCleared() {
        stopTracking()
        super.onCleared()
    }
}