package com.example.skipthejam.viewmodel

import android.net.Uri
import com.example.skipthejam.model.EventType
import com.example.skipthejam.utils.StorageHelper
import com.google.android.gms.tasks.Tasks
import com.example.skipthejam.model.Location as MyLocation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MyLocationViewModel {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private val _locations = MutableStateFlow<List<MyLocation>>(emptyList())
    val locations: StateFlow<List<MyLocation>> = _locations

    fun saveLocation(type: EventType,
                     image: Uri? ,
                     description: String,
                     currentLocation: android.location.Location?,
                     onResult: (Boolean, String?)->Unit
    )
    {
        val uid = auth.currentUser?.uid ?: run {
            onResult(false, "Korisnik nije prijavljen?")
            return
        }
        if(currentLocation==null)
            onResult(false, "Trenutna lokacija je nepoznata")

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
                if(image!=null){
                    val locationId = docRef.id
                    val path = "locations_images/$locationId/${uid}_${System.currentTimeMillis()}.jpg"
                    StorageHelper.uploadFile(image, path){ sucess, downloadUrl ->
                        if(sucess && downloadUrl != null){
                            db.collection("locations")
                                .document(locationId)
                                .update("imageUrl", downloadUrl)
                                .addOnSuccessListener { onResult(true, "Dodata je slika lokacije") }
                                .addOnFailureListener { onResult(false, "Lokacija dodata, slika nije") }
                        }
                        else
                            onResult(false, "Nije moguće uploadovati sliku")
                    }
                }
                onResult(true, "Lokacija uspešno dodata")
            }
            .addOnFailureListener { onResult(false, "Greška prilikom dodavanja lokacije")}
    }

    fun deleteLocation(locationId: String, onResult: (Boolean, String?) -> Unit) {
        deleteCommentsForLocation(locationId) { success, msg ->
            if (!success) {
                onResult(false, "Greška pri brisanju komentara: $msg")
                return@deleteCommentsForLocation
            }

            val folderRef = storage.reference.child("locations/$locationId")
            folderRef.listAll().addOnSuccessListener { listResult ->
                val deleteTasks = listResult.items.map { it.delete() }

                Tasks.whenAll(deleteTasks).addOnSuccessListener {
                    db.collection("locations").document(locationId)
                        .delete()
                        .addOnSuccessListener { onResult(true, "Lokacija i sve slike uspešno obrisane") }
                        .addOnFailureListener { onResult(false, "Greška pri brisanju lokacije") }
                }
                    .addOnFailureListener { onResult(false, "Greška pri brisanju slika") }
            }
                .addOnFailureListener { onResult(false, "Greška pri pristupu folderu slika") }
        }
    }

        public fun deleteCommentsForLocation(locationId: String, onResult: (Boolean, String?) -> Unit) {
            // dodati u commentsviewmodel
            onResult(true, null)
        }
}