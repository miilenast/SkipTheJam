package com.example.skipthejam.viewmodel

import android.app.Application
import android.location.Location
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import com.example.skipthejam.service.PointsService
import com.example.skipthejam.service.CommentsService
import com.example.skipthejam.model.EventType
import com.example.skipthejam.service.LocationService
import com.example.skipthejam.utils.StorageHelper
import com.google.android.gms.tasks.Tasks
import com.example.skipthejam.model.Location as MyLocation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MyLocationsViewModel(application: Application): AndroidViewModel(application) {
    private val locationService = LocationService(application.applicationContext)
    private val commentsService = CommentsService(application.applicationContext)
    private val pointsService = PointsService()
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private val _locations = MutableStateFlow<List<MyLocation>>(emptyList())
    val locations: StateFlow<List<MyLocation>> = _locations

    private val _filteredLocations = MutableStateFlow<List<MyLocation>>(emptyList())
    val filteredLocations: StateFlow<List<MyLocation>> = _filteredLocations

    fun saveLocation(
        type: EventType,
        image: Uri?,
        description: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        val uid = auth.currentUser?.uid ?: run {
            onResult(false, "Korisnik nije prijavljen?")
            return
        }
        val currentLocation = locationService.getCurrentLocation()
        if (currentLocation == null) {
            onResult(false, "Trenutna lokacija je nepoznata")
            return
        }
        val location = MyLocation(
            type = type,
            imageUrl = null,
            description = description,
            uid = uid,
            latitude = currentLocation!!.latitude,
            longitude = currentLocation.longitude
        )
        db.collection("locations")
            .add(location)
            .addOnSuccessListener { docRef ->

                if (image != null) {
                    val locationId = docRef.id
                    val path =
                        "locations_images/$locationId/${uid}_${System.currentTimeMillis()}.jpg"
                    StorageHelper.uploadFile(image, path) { sucess, downloadUrl ->
                        if (sucess && downloadUrl != null) {
                            db.collection("locations")
                                .document(locationId)
                                .update("imageUrl", downloadUrl)
                                .addOnSuccessListener {
                                    pointsService.addPointsToCurrentsUser(5)
                                    onResult(true, "Dodata je lokacija sa slikom, +5p")

                                }
                                .addOnFailureListener {
                                    pointsService.addPointsToCurrentsUser(3)
                                    onResult( false, "Lokacija dodata , slika nije, +3p" )
                                }
                        } else {
                            pointsService.addPointsToCurrentsUser(3)
                            onResult(false, "Nije moguće uploadovati sliku, +3p")
                        }
                    }
                } else
                    onResult(true, "Lokacija uspešno dodata")
            }
            .addOnFailureListener { onResult(false, "Greška prilikom dodavanja lokacije") }
    }

    fun fetchAllLocations() {
        db.collection("locations")
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener
                if (snapshot != null) {
                    val locs = snapshot.toObjects(MyLocation::class.java)
                    _locations.value = locs
                }
            }
    }

    init {
        fetchAllLocations()
    }

    private var activeFilterType: EventType? = null
    private var activeFilterAuthorId: String? = null
    private var activeFilterLastUpdate: Long? = null
    private var activeFilterRadius: Float? = null

    private fun applyFilters(lastComment: Map<String, Long?>): List<MyLocation> {
        var filtered = _locations.value

        activeFilterType?.let { type ->
            filtered = filtered.filter { it.type == type }
        }
        activeFilterAuthorId.let { authorid ->
            filtered = filtered.filter { it.uid == authorid }
        }
        activeFilterLastUpdate?.let { timeMills ->
            filtered = filtered.filter { location ->
                var lastUpdate = lastComment[location.id] ?: location.timestamp
                lastUpdate >= System.currentTimeMillis() - timeMills
            }
        }
        val currentLocation = locationService.getCurrentLocation()
        activeFilterRadius?.let { radius ->
            filtered = filtered.filter { location ->
                val result = FloatArray(1)
                Location.distanceBetween(
                    currentLocation!!.latitude, currentLocation.longitude,
                    location.latitude, location.longitude,
                    result
                )
                result[0] <= radius
            }
        }

        _filteredLocations.value = filtered
        return filtered
    }

    fun filterByAuthor(authorId: String) {
        if (activeFilterAuthorId == null || activeFilterAuthorId != authorId) {
            db.collection("users").document(authorId).get()
                .addOnSuccessListener { activeFilterAuthorId = authorId }
                .addOnFailureListener { activeFilterAuthorId = null }
            applyFilter()
        }
    }

    fun filterByType(type: EventType) {
        if (activeFilterType == null || activeFilterType != type) {
            activeFilterType = type
            applyFilter()
        }
    }

    fun filteredByLastUpdate(timeMills: Long) {
        activeFilterLastUpdate = timeMills
        applyFilter()
    }

    fun filterByRadius(radius: Float) {
        activeFilterRadius = radius
        applyFilter()
    }

    fun applyFilter() {
        commentsService.getLastComments(){ _, lastcomments ->
            applyFilters( lastcomments )
        }
    }

    fun resetFilters() {
        activeFilterType = null
        activeFilterRadius = null
        activeFilterAuthorId = null
        activeFilterLastUpdate = null
    }
}

//    fun filteredByAuthor(authorId: String, onResult: (Boolean, List<MyLocation>?) -> Unit){
//        db.collection("users").document(authorId).get()
//            .addOnSuccessListener { doc ->
//                if(!doc.exists())
//                    onResult(false, null)
//                else{
//                    val filtered = _locations.value.filter { it.uid == authorId }
//                    _filteredLocations.value = filtered
//                    onResult(true, filtered)
//                }
//            }
//            .addOnFailureListener { onResult(false, null) }
//    }

//    fun filteredByType(type: EventType, onResult: (Boolean, List<MyLocation>) -> Unit)
//    {
//        val filtered = _locations.value.filter { it.type == type }
//        _filteredLocations.value = filtered;
//        onResult(true, filtered)
//    }
//
//    fun filteredByLastUpdate(timeMills: Long, onResult: (Boolean, List<MyLocation>?) -> Unit){
//        val filtered = mutableListOf<MyLocation>()
//        val locationList = _locations.value
//        var remaining = locationList.size
//        if(locationList.isEmpty()) onResult(true, emptyList())
//
//        for(location in locationList){
//            commentsService.getComments(location.id) { success, comments ->
//                if(!success) {
//                    onResult(false, null)
//                    return@getComments
//                }
//                val lastUpdate = if (comments.isNotEmpty())
//                    comments.maxOf { it.timestamp }
//                else
//                    location.timestamp
//
//                if(lastUpdate >= System.currentTimeMillis() - timeMills)
//                    filtered.add(location)
//
//                remaining--
//                if(remaining == 0){
//                    _filteredLocations.value = filtered
//                    onResult(true, filtered)
//                }
//            }
//        }
//    }
//
//    fun filterByRadius(radius: Int, onResult: (Boolean, List<MyLocation>?) -> Unit)
//    {
//        val currentLocation = locationService.getCurrentLocation()
//        if(currentLocation == null){
//            onResult(false, emptyList())
//            return
//        }
//        val filtered = _locations.value.filter { location ->
//            val result = FloatArray(1)
//            Location.distanceBetween(
//                currentLocation!!.latitude, currentLocation.longitude,
//                location.latitude, location.longitude,
//                result
//            )
//            result[0] <= radius
//        }
//        _filteredLocations.value = filtered
//        onResult(true, filtered)
//    }