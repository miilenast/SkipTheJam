package com.example.skipthejam.model

import com.google.firebase.firestore.DocumentId

data class Location(
    @DocumentId
    val id: String = "",
    val type: EventType = EventType.TRAFFIC_JAM,
    val imageUrl: String? = null,
    val description: String = "",
    val uid: String,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis(),
    )