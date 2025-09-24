package com.example.skipthejam.viewmodel

import android.app.Application
import android.location.Location
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.example.skipthejam.service.PointsService
import com.example.skipthejam.service.CommentsService
import com.example.skipthejam.model.EventType
import com.example.skipthejam.utils.StorageHelper
import com.example.skipthejam.model.Location as MyLocation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MyLocationsViewModel(application: Application): AndroidViewModel(application) {
    var currentLocation: Location? = null
    private val commentsService = CommentsService()
    private val pointsService = PointsService()
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _locations = MutableStateFlow<List<MyLocation>>(emptyList())
    private val _filteredLocations = MutableStateFlow<List<MyLocation>>(emptyList())
    val filteredLocations: StateFlow<List<MyLocation>> = _filteredLocations

    fun saveLocation(
        type: EventType,
        image: Uri?,
        description: String,
        latitude: Double,
        longitude: Double,
        onResult: (Boolean, String?) -> Unit
    ) {
        val uid = auth.currentUser?.uid ?: run {
            onResult(false, "Korisnik nije prijavljen?")
            return
        }

        db.collection("users").document(uid).get()
            .addOnSuccessListener { snapshot ->
                val username = snapshot.getString("username") ?: "nepoznato"

                val location = MyLocation(
                    type = type.eventName,
                    imageUrl = null,
                    description = description,
                    uid = uid,
                    username = username,
                    latitude = latitude,
                    longitude = longitude
                )

                db.collection("locations")
                    .add(location)
                    .addOnSuccessListener { docRef ->

                        val locationId = docRef.id
                        db.collection("locations").document(locationId)
                            .update("id", locationId)

                        if (image != null) {
                            val path =
                                "locations_images/$locationId/${uid}_${System.currentTimeMillis()}.jpg"
                            StorageHelper.uploadFile(image, path) { success, downloadUrl ->
                                if (success && downloadUrl != null) {
                                    db.collection("locations")
                                        .document(locationId)
                                        .update("imageUrl", downloadUrl)
                                        .addOnSuccessListener {
                                            pointsService.addPointsToCurrentsUser(5)
                                            onResult(true, "Dodata je lokacija sa slikom, +5p")

                                        }
                                        .addOnFailureListener { e ->
                                            pointsService.addPointsToCurrentsUser(3)
                                            onResult(false, "Lokacija dodata , slika nije, +3p")
                                        }
                                } else {
                                    pointsService.addPointsToCurrentsUser(3)
                                    onResult(false, "Nije moguće uploadovati sliku, +3p")
                                }
                            }
                        } else {
                            pointsService.addPointsToCurrentsUser(3)
                            onResult(true, "Lokacija uspešno dodata, +3p")
                        }
                    }
                    .addOnFailureListener { onResult(false, "Greška prilikom dodavanja lokacije") }
            }
            .addOnFailureListener {
                onResult(false, "Greška pri čitanju korisnika")
            }
    }

    fun fetchAllLocations() {
        db.collection("locations")
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener
                if (snapshot != null) {
                    val locs = snapshot.toObjects(MyLocation::class.java)
                    _locations.value = locs
                    applyFilter()
                }
            }
    }

    init {
        fetchAllLocations()
    }

    private var activeFilterType: EventType? = null
    private var activeFilterAuthor: String? = null
    private var activeFilterLastUpdate: Long? = null
    var activeFilterRadius: Float? = null


    private fun applyFilters(lastComment: Map<String, Long?>): List<MyLocation> {
        var filtered = _locations.value

        activeFilterType?.let { type ->
            filtered = filtered.filter { it.type == type.eventName }
        }
        activeFilterAuthor?.let { author ->
            filtered = filtered.filter { it.username == author }
        }
        activeFilterLastUpdate?.let { timeMills ->
            filtered = filtered.filter { location ->
                val lastUpdate = lastComment[location.id] ?: location.timestamp
                lastUpdate >= System.currentTimeMillis() - timeMills
            }
        }
        val currLocation = currentLocation
        if(currLocation!=null) {
            activeFilterRadius?.let { radius ->
                val radiusMeters = radius * 1000
                Log.d("RADIJUS", radiusMeters.toString())
                filtered = filtered.filter { location ->
                    val result = FloatArray(1)
                    Location.distanceBetween(
                        currLocation.latitude, currLocation.longitude,
                        location.latitude, location.longitude,
                        result
                    )
                    result[0] <= radiusMeters
                }
            }
        }

        _filteredLocations.value = filtered
        return filtered
    }

    fun filterByAuthor(author: String) {
        if (activeFilterAuthor == null || activeFilterAuthor != author) {
            activeFilterAuthor = author
            applyFilter()
        }
    }

    fun filterByType(type: EventType) {
        if (activeFilterType == null || activeFilterType != type) {
            activeFilterType = type
            applyFilter()
        }
    }

    fun filterByLastUpdate(timeMills: Long) {
        activeFilterLastUpdate = timeMills
        applyFilter()
    }

    fun filterByRadius(radius: Float) {
        activeFilterRadius = radius
        applyFilter()
    }

    fun applyFilter() {
        commentsService.getLastComments { _, lastcomments ->
            applyFilters( lastcomments )
        }
    }

    fun resetFilters() {
        activeFilterType = null
        activeFilterRadius = null
        activeFilterAuthor = null
        activeFilterLastUpdate = null
        applyFilter()
    }

    fun apllyUserFilters(
        author: String? = null,
        type: EventType? = null,
        time: Long? = null,
        radiusKm: Float? = null
    ){
        resetFilters()

        if(!author.isNullOrBlank())
            filterByAuthor(author)
        if(type!=null)
            filterByType(type)
        if(time!=null)
            filterByLastUpdate(time)
        if(radiusKm!=null)
            filterByRadius(radiusKm)
    }
}