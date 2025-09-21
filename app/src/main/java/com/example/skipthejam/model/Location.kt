package com.example.skipthejam.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties

data class Location(
    val id: String = "",
    val type: String = EventType.TRAFFIC_JAM.eventName,
    val imageUrl: String? = null,
    val description: String = "",
    val uid: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis(),
    )