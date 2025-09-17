package com.example.skipthejam.model

import com.google.firebase.firestore.DocumentId

data class Comment(
    @DocumentId
    val locationid: String = "",
    val uid: String = "",
    val description: String = "",
    val imageUrl: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
)