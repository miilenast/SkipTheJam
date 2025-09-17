package com.example.skipthejam.model

import com.google.firebase.firestore.DocumentId

data class User(
    val id: String = "",
    val ime: String = "",
    val prezime: String = "",
    val brojTelefona: String = "",
    val email: String = "",
    val username: String = "",
    val profilnaSlikaURL: String = "",
    val poeni: Int = 0
)