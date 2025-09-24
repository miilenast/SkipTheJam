//package com.example.skipthejam.service
//
//import android.Manifest
//import android.content.Context
//import com.google.android.gms.location.*
//import android.content.pm.PackageManager
//import android.os.Looper
//import androidx.core.app.ActivityCompat
//import android.location.Location
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.firestore.FirebaseFirestore
//
//class LocationService(private val context: Context) {
//    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
//    private var locationCallback: LocationCallback? = null
//    var onLocation: ((Location) -> Unit)? = null
//    private var lastLocation: Location? = null
//
//    private val db = FirebaseFirestore.getInstance()
//    private val auth = FirebaseAuth.getInstance()
//
//    fun startLocationUpdates(){
//        if(locationCallback != null) return
//
//        val request = LocationRequest.Builder(
//            Priority.PRIORITY_HIGH_ACCURACY,
//            5000L
//        )
//            .setMinUpdateIntervalMillis(2000L)
//            .setWaitForAccurateLocation(true)
//            .build()
//
//        locationCallback = object : LocationCallback() {
//            override fun onLocationResult(result: LocationResult) {
//                result.lastLocation?.let { loc ->
//                    onLocation?.invoke(loc)
//
//                    val uid = auth.currentUser?.uid ?: return
//                    val userRef = db.collection("users").document(uid)
//                    val updates = mapOf(
//                        "latitude" to loc.latitude,
//                        "longitude" to loc.longitude,
//                        "timestamp" to System.currentTimeMillis()
//                    )
//                    userRef.update(updates)
//
//                    lastLocation = loc
//                }
//            }
//        }
//
//        if (ActivityCompat.checkSelfPermission(
//                context,
//                Manifest.permission.ACCESS_FINE_LOCATION
//            ) == PackageManager.PERMISSION_GRANTED
//        ) {
//            fusedLocationClient.requestLocationUpdates(
//                request,
//                locationCallback!!,
//                Looper.getMainLooper()
//            )
//        }
//    }
//
//    fun stopLocationUpdates() {
//        locationCallback?.let {
//            fusedLocationClient.removeLocationUpdates(it)
//        }
//        locationCallback = null
//    }
//}