package com.example.skipthejam.service

import android.content.Context
import com.example.skipthejam.model.Comment
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject

class CommentsService(private val context: Context) {
    private val db = FirebaseFirestore.getInstance()

    fun getComments(locationId: String, onResult: (Boolean, List<Comment>?) -> Unit) {
        db.collection("comments")
            .whereEqualTo("locationid", locationId)
            .get()
            .addOnSuccessListener { result ->
                val comments = result.map { doc -> doc.toObject(Comment::class.java) }
                onResult(true, comments)
            }
            .addOnFailureListener { onResult(false, null) }
    }

    fun getLastComments(onResult: (Boolean, Map<String, Long>) -> Unit) {
        db.collection("comments")
            .get()
            .addOnSuccessListener { result ->
                val lastComments = mutableMapOf<String, Long>()
                for (doc in result) {
                    val comment = doc.toObject(Comment::class.java)
                    val current = lastComments[comment.locationid] ?: 0
                    if (comment.timestamp > current)
                        lastComments[comment.locationid] = comment.timestamp
                }
                onResult(true, lastComments)
            }
            .addOnFailureListener { onResult(false, emptyMap()) }
    }

    fun deleteCommentsForLocation(locationId: String, onResult: (Boolean, String?) -> Unit) {
        db.collection("comments")
            .whereEqualTo("locationid", locationId)
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