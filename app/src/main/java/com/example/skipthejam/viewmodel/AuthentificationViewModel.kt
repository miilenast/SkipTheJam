package com.example.skipthejam.viewmodel

import com.example.skipthejam.utils.StorageHelper
import com.example.skipthejam.model.User
import android.net.Uri
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class AuthentificationViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val _currentUser = mutableStateOf<FirebaseUser?>(auth.currentUser)
    val currentUser: State<FirebaseUser?> = _currentUser
    private val _currentUserUser = mutableStateOf<User?>(null)
    val currentUserUser: State<User?> = _currentUserUser

    fun registerUser(username: String,
                     email: String, password: String,
                     ime: String, prezime: String,
                     brojTelefona: String,
                     profilnaSlika: Uri?,
                     onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            if (ime.isEmpty() || prezime.isEmpty() || username.isEmpty() || brojTelefona.isEmpty()) {
                onResult(false, "Potrebno je uneti sve korisničke podatke")
                return@launch
            }

            db.collection("users")
                .whereEqualTo("username", username)
                .get()
                .addOnSuccessListener { result ->
                    if (!result.isEmpty) {
                        onResult(false, "Ovo korisničko ime je zauzeto")
                        return@addOnSuccessListener
                    }
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                _currentUser.value = auth.currentUser
                                val uid = auth.currentUser!!.uid

                                var user = User(
                                    id = uid,
                                    username = username,
                                    ime = ime,
                                    prezime = prezime,
                                    brojTelefona = brojTelefona,
                                    email = email,
                                    profilnaSlikaURL = ""
                                )
                                db.collection("users")
                                    .document(uid)
                                    .set(user)
                                    .addOnSuccessListener {
                                        _currentUserUser.value = user
                                        profilnaSlika?.let { uri ->
                                            val path = "profile_pictures/$uid.jpg"
                                            StorageHelper.uploadFile(uri, path) { success, downloadUrl ->
                                                if(success && downloadUrl != null){
                                                    db.collection("users")
                                                        .document(uid)
                                                        .update("profilnaSlikaURL", downloadUrl)
                                                    user = user.copy(profilnaSlikaURL = downloadUrl)
                                                    _currentUserUser.value = user
                                                    onResult(true, "Profilna slika uploadovana")
                                                }
                                                else
                                                    onResult(false, "Nije moguce uploadovati profilnu sliku")
                                            }
                                        } ?: run {
                                            onResult(true, "Uspešno ste se registrovali")
                                        }
                                    }
                                    .addOnFailureListener { e -> onResult(false, e.message) }
                            }
                            else{
                                onResult(false, task.exception?.message)
                                return@addOnCompleteListener
                            }
                    }
                }
                .addOnFailureListener { e -> onResult(false, e.message) }
        }
    }

    fun loginUser(username: String, password: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch{
            db.collection("users")
                .whereEqualTo("username", username)
                .get()
                .addOnSuccessListener { result ->
                    if(result.isEmpty)
                        onResult(false, "Dato korisničko ime ne postoji")
                    else{
                        val userDocument = result.documents[0]
                        val email = userDocument.getString("email") ?: ""
                        auth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if(task.isSuccessful) {
                                    val fetchedUser = User(
                                        id = userDocument.id,
                                        username = userDocument.getString("username") ?: "",
                                        ime = userDocument.getString("ime") ?: "",
                                        prezime = userDocument.getString("prezime") ?: "",
                                        brojTelefona = userDocument.getString("brojTelefona") ?: "",
                                        profilnaSlikaURL = userDocument.getString("profilnaSlikaURL") ?: ""
                                    )
                                    _currentUser.value = auth.currentUser
                                    _currentUserUser.value = fetchedUser
                                    onResult(true, "Uspešno ste se prijavili")
                                }
                                else
                                    onResult(false, task.exception?.message)
                            }
                    }
                }
                .addOnFailureListener{ e -> onResult(false, e.message) }
        }
    }

    fun logoutUser(onResult: (Boolean, String?) -> Unit) {
        try {
            auth.signOut()
            _currentUser.value = null
            _currentUserUser.value = null
            onResult(true, "Uspešno ste odjavljeni")
        }
        catch(e: Exception){
            onResult(false, e.message)
        }
    }
    init{
        _currentUser.value?.uid?.let { uid ->
            db.collection("users").document(uid)
                .get()
                .addOnSuccessListener { snapshot ->
                    _currentUserUser.value = snapshot.toObject(User::class.java)
                }
        }
    }
}