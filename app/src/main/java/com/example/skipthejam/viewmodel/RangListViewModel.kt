package com.example.skipthejam.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.skipthejam.model.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class RangListViewModel:ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    private val _topUsers = MutableLiveData<List<User>>(emptyList())
    val topUsers: LiveData<List<User>> = _topUsers

    fun fetchTopUsers(limit: Int = 10){
        viewModelScope.launch(Dispatchers.IO) {
            try{
                val snapshot = db.collection("users")
                    .orderBy("poeni", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .limit(limit.toLong())
                    .get()
                    .await()
                val users = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(User::class.java)?.copy(id = doc.id)
                }
                _topUsers.postValue(users)
            }
            catch (e: Exception) {
                _topUsers.postValue(emptyList())
            }
        }
    }
}