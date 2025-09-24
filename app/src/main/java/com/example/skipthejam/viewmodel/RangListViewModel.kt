package com.example.skipthejam.viewmodel

import androidx.lifecycle.ViewModel
import com.example.skipthejam.model.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class RangListViewModel:ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    private val _topUsers = MutableStateFlow<List<User>>(emptyList())
    val topUsers: StateFlow<List<User>> = _topUsers
    private var usersListener: ListenerRegistration? = null

    init{
        fetchTopUsers()
    }

    fun fetchTopUsers(limit: Int = 10){
        usersListener?.remove()
        usersListener = db.collection("users")
            .orderBy("poeni", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    _topUsers.value = emptyList()
                    return@addSnapshotListener
                }

                val users = snapshot.mapNotNull { doc ->
                    doc.toObject(User::class.java).copy(id = doc.id)
                }
                _topUsers.value = users
            }
    }
}