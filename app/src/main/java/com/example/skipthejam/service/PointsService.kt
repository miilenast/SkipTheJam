package com.example.skipthejam.service

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class PointsService {
    private val db = FirebaseFirestore.getInstance();
    private val auth = FirebaseAuth.getInstance()

    fun addPointsToCurrentsUser(points: Int){
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid)
            .update("poeni", FieldValue.increment(points.toLong()))
    }
}