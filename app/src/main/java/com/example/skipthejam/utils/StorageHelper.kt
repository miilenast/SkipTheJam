package com.example.skipthejam.utils

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage

object StorageHelper {
    private val storage = FirebaseStorage.getInstance()

    fun uploadFile(uri: Uri, path: String, onResult: (Boolean, String?) -> Unit) {
        val ref = storage.reference.child(path)
        ref.putFile(uri)
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { downloadUri ->
                    onResult(true, downloadUri.toString())
                }
            }
            .addOnFailureListener { e-> onResult(false, e.message) }
    }
}