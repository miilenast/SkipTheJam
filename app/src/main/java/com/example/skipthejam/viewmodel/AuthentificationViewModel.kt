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
                    if (!result.isEmpty)
                        onResult(false, "Ovo korisničko ime je zauzeto")
                    else {
                        auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    _currentUser.value = auth.currentUser
                                    val uid = auth.currentUser!!.uid

                                    var user = User(
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
                                        .addOnSuccessListener { onResult(true, "Uspešno ste se registrovali") }
                                        .addOnFailureListener { e -> onResult(false, e.message) }

                                    profilnaSlika?.let { uri ->
                                        val path = "profile_pictures/$uid.jpg"
                                        StorageHelper.uploadFile(uri, path) { success, downloadUrl ->
                                            if(success && downloadUrl != null){
                                                db.collection("users")
                                                    .document(uid)
                                                    .update("profilnaSlikaURL", downloadUrl)
                                                user = user.copy(profilnaSlikaURL = downloadUrl)
                                                onResult(true, "Profilna slika uploadovana")
                                            }
                                            else
                                                onResult(false, "Nije moguce uploadovati profilnu sliku")
                                        }
                                    }
                                }
                                else
                                    onResult(false, task.exception?.message)
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
                                    _currentUser.value = auth.currentUser
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
            onResult(true, "Uspešno ste odjavljeni")
        }
        catch(e: Exception){
            onResult(false, e.message)
        }
    }
}