package com.example.skipthejam.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.skipthejam.model.Comment
import com.example.skipthejam.model.Location
import com.example.skipthejam.service.CommentsService
import com.example.skipthejam.service.LocationService
import com.example.skipthejam.service.PointsService
import com.example.skipthejam.utils.StorageHelper
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.example.skipthejam.model.Location as MyLocation

class PostViewModel(application: Application): AndroidViewModel(application){
    private val commentsService = CommentsService(application.applicationContext)
    private val pointsService = PointsService()
    val db = FirebaseFirestore.getInstance()
    val storage = FirebaseStorage.getInstance()
    val auth = FirebaseAuth.getInstance()

    private val _selectedLocation = MutableStateFlow<MyLocation?>(null)
    val selectedLocation: StateFlow<MyLocation?> = _selectedLocation
    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    val comment: StateFlow<List<Comment>> = _comments

    fun selectLocation(locationId: String){
        viewModelScope.launch {
            db.collection("locations")
                .document(locationId)
                .get()
                .addOnSuccessListener { doc ->
                    val location = doc.toObject(Location::class.java)
                    if(location != null){
                        _selectedLocation.value = location
                        commentsService.getComments(locationId) {success, comments ->
                            if(success)
                                _comments.value = comments!!.sortedByDescending { it.timestamp }
                            else
                                _comments.value = emptyList()
                        }
                    }
                    else
                        deselectLocation()
                }
                .addOnFailureListener { deselectLocation() }
        }
    }

    fun deselectLocation() {
        _selectedLocation.value = null
        _comments.value = emptyList()
    }

    fun addComment(description: String,
                   imageUri: Uri? = null,
                   onResult: (Boolean, String?) -> Unit) {
        val uid = auth.currentUser?.uid
        if( uid == null){
            onResult(false, "Korisnik nije prijavljen")
            return
        }

        val location = _selectedLocation.value
        if (location == null) {
            onResult(false, "lokacija nije selektovana")
            return
        }

        db.collection("users").document(uid)
            .get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) {
                    onResult(false, "Korisnik ne postoji")
                    return@addOnSuccessListener
                }

                val comment = Comment(
                    locationid = location.id,
                    uid = uid,
                    description = description,
                    imageUrl = null
                )

                db.collection("comments")
                    .add(comment)
                    .addOnSuccessListener { docRef ->
                        if (imageUri != null) {
                            val path = "locations/${location.id}/${uid}_${System.currentTimeMillis()}.jpg"
                            StorageHelper.uploadFile(imageUri, path) { success, downloadUrl ->
                                if (success && downloadUrl != null) {
                                    docRef.update("imageUrl", downloadUrl)
                                        .addOnSuccessListener {
                                            pointsService.addPointsToCurrentsUser(3)
                                            onResult(true, "Dodat komentar sa slikom, +3p")
                                        }
                                        .addOnFailureListener {
                                            pointsService.addPointsToCurrentsUser(2)
                                            onResult( false, "Komentar bez slike, +2p" )
                                        }
                                } else{
                                    pointsService.addPointsToCurrentsUser(2)
                                    onResult(true, "Dodat komentar, slika nije uploadovana")
                                }}
                        } else {
                            pointsService.addPointsToCurrentsUser(2)
                            onResult(true, "Dodat komentar")
                        }
                    }
                    .addOnFailureListener { onResult(false, "komentar nije dodat") }
            }
            .addOnFailureListener {onResult(false, "ne postoji korisnik s ovim id-jem")}
    }

    fun deleteLocation(onResult: (Boolean, String?) -> Unit) {
        val uid = auth.currentUser?.uid
        if( uid == null){
            onResult(false, "Korisnik nije prijavljen")
            return
        }
        val location = _selectedLocation.value
        if(location == null){
            onResult(false, "Nije izabrana lokacija")
            return
        }
        val locationId = location.id;

        commentsService.deleteCommentsForLocation(locationId) { success, msg ->
            if (!success) {
                onResult(false, "Greška pri brisanju komentara: $msg")
                return@deleteCommentsForLocation
            }

            val folderRef = storage.reference.child("locations/$locationId")
            folderRef.listAll()
                .addOnSuccessListener { listResult ->
                    val deleteTasks = listResult.items.map { it.delete() }

                    Tasks.whenAll(deleteTasks)
                        .addOnSuccessListener {
                            db.collection("locations").document(locationId)
                                .delete()
                                .addOnSuccessListener {
                                    pointsService.addPointsToCurrentsUser(4)
                                    _selectedLocation.value = null;
                                    _comments.value = emptyList()
                                    onResult( true, "Lokacija i sve slike uspešno obrisane" )
                                }
                                .addOnFailureListener {
                                    onResult( false, "Greška pri brisanju lokacije" )
                                }
                        }
                        .addOnFailureListener { onResult(false, "Greška pri brisanju slika") }
                }
                .addOnFailureListener { onResult(false, "Greška pri pristupu folderu slika") }
        }
    }
}