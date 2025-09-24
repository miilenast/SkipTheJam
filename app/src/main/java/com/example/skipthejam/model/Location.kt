package com.example.skipthejam.model

data class Location(
    val id: String = "",
    val type: String = EventType.TRAFFIC_JAM.eventName,
    val imageUrl: String? = null,
    val description: String = "",
    val uid: String = "",
    val username: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis(),
    )