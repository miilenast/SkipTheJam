package com.example.skipthejam.service

import android.content.Context
import com.example.skipthejam.model.Comment
import com.google.firebase.firestore.FirebaseFirestore

class CommentsService() {
    private val db = FirebaseFirestore.getInstance()

    fun getLastComments(onResult: (Boolean, Map<String, Long>) -> Unit) {
        db.collection("comments")
            .get()
            .addOnSuccessListener { result ->
                val lastComments = mutableMapOf<String, Long>()
                for (doc in result) {
                    val comment = doc.toObject(Comment::class.java)
                    val current = lastComments[comment.locationId] ?: 0
                    if (comment.timestamp > current)
                        lastComments[comment.locationId] = comment.timestamp
                }
                onResult(true, lastComments)
            }
            .addOnFailureListener { onResult(false, emptyMap()) }
    }

    fun deleteCommentsForLocation(locationId: String, onResult: (Boolean, String?) -> Unit) {
        db.collection("comments")
            .whereEqualTo("locationId", locationId)
            .get()
            .addOnSuccessListener { result ->
                val batch = db.batch()
                for (doc in result)
                    batch.delete(doc.reference)
                batch.commit()
                    .addOnSuccessListener { onResult(true, "Obrisani svi komentari za objavu") }
                    .addOnFailureListener { onResult(false, "Komentari nisu obrisani") }
            }
            .addOnFailureListener { onResult(false, "Nema komentara za ovu objavu") }
    }
}