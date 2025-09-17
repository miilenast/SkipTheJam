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
}
//    fun fetchLocations(onResult: ((Boolean, String?) -> Unit)? = null) {
//        db.collection("locations")
//            .get()
//            .addOnSuccessListener { result ->
//                _locations.clear()
//                for (doc in result) {
//                    val location = doc.toObject(MyLocation::class.java)
//                    _locations.add(location)
//                }
//                onResult?.invoke(true, null)
//            }
//            .addOnFailureListener { e ->
//                onResult?.invoke(false, e.message)
//            }
//    }
//
//    // --- Dodavanje lokacije sa opcionom slikom ---
//    fun addLocation(
//        location: MyLocation,
//        imageUri: Uri? = null,
//        onResult: (Boolean, String?) -> Unit
//    ) {
//        val uid = db.collection("locations").document().id
//        val newLocation = location.copy(id = uid, timestamp = System.currentTimeMillis())
//
//        if (imageUri != null) {
//            val path = "location_images/$uid.jpg"
//            StorageHelper.uploadFile(imageUri, path) { success, downloadUrl ->
//                val locWithImage = if (success && downloadUrl != null) {
//                    newLocation.copy(imageUrl = downloadUrl)
//                } else newLocation
//                saveLocation(locWithImage, onResult)
//            }
//        } else {
//            saveLocation(newLocation, onResult)
//        }
//    }
//
//    // --- Privatna funkcija za čuvanje lokacije ---
//    private fun saveLocation(location: MyLocation, onResult: (Boolean, String?) -> Unit) {
//        db.collection("locations")
//            .document(location.id)
//            .set(location)
//            .addOnSuccessListener {
//                _locations.add(location)
//                onResult(true, "Lokacija uspešno dodata")
//            }
//            .addOnFailureListener { e ->
//                onResult(false, e.message)
//            }
//    }
//
//    // --- Kreiranje lokacije na trenutnoj poziciji korisnika ---
//    fun addLocationAtCurrentPosition(
//        authorUsername: String,
//        type: EventType = EventType.TRAFFIC_JAM,
//        description: String = "",
//        imageUri: Uri? = null,
//        onResult: (Boolean, String?) -> Unit
//    ) {
//        val loc = _currentLocation.value
//        if (loc == null) {
//            onResult(false, "Trenutna lokacija nije dostupna")
//            return
//        }
//        val location = MyLocation(
//            latitude = loc.latitude,
//            longitude = loc.longitude,
//            authorUsername = authorUsername,
//            type = type,
//            description = description
//        )
//        addLocation(location, imageUri, onResult)
//    }
//
//    // --- Filtriranje po radijusu ---
//    fun getLocationsNearUser(userLat: Double, userLng: Double, radiusKm: Double): List<MyLocation> {
//        return _locations.filter { location ->
//            distanceInKm(userLat, userLng, location.latitude, location.longitude) <= radiusKm
//        }
//    }
//
//    // --- Filtriranje po tipu, autoru, datumu ---
//    fun filterLocations(
//        type: EventType? = null,
//        authorUsername: String? = null,
//        startTime: Long? = null,
//        endTime: Long? = null
//    ): List<MyLocation> {
//        return _locations.filter { location ->
//            (type == null || location.type == type) &&
//                    (authorUsername == null || location.authorUsername == authorUsername) &&
//                    (startTime == null || location.timestamp >= startTime) &&
//                    (endTime == null || location.timestamp <= endTime)
//        }
//    }
//
//    // --- Haversine formula za izračunavanje distance ---
//    private fun distanceInKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
//        val R = 6371.0 // Zemljin radijus u km
//        val dLat = Math.toRadians(lat2 - lat1)
//        val dLon = Math.toRadians(lon2 - lon1)
//        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
//                Math.cos(Math.toRadians(lat1)) *
//                Math.cos(Math.toRadians(lat2)) *
//                Math.sin(dLon / 2) * Math.sin(dLon / 2)
//        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
//        return R * c
//    }
}