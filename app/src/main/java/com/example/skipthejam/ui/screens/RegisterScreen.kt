package com.example.skipthejam.ui.screens

import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.skipthejam.viewmodel.AuthentificationViewModel
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.rememberAsyncImagePainter
import java.io.File
import java.io.FileOutputStream
import android.Manifest
import android.content.pm.PackageManager

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    authViewModel: AuthentificationViewModel
){
    var ime by remember { mutableStateOf("") }
    var prezime by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var brojTelefona by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var profilePicture by remember { mutableStateOf<Uri?>(null) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri:Uri? -> profilePicture = uri }

    val context = LocalContext.current
    var hasCameraPermision by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA)
        == PackageManager.PERMISSION_GRANTED
        ) }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        bitmap?.let {
            try {
                val file = File(context.cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
                FileOutputStream(file).use { out ->
                    it.compress(Bitmap.CompressFormat.JPEG, 100, out)
                }
                val uri = Uri.fromFile(file)
                Log.d("Camera Capture", "Kreiran URI: $uri")
                profilePicture = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            }
            catch (e: Exception){
                Log.e("Camera Capture", "Greška pri čuvanju slike", e)
            }
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermision = granted
        if(granted)
            cameraLauncher.launch(null)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Registracija",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ){
            if(profilePicture!=null){
                Image(
                    painter = rememberAsyncImagePainter(profilePicture),
                    contentDescription = "Profilna slika",
                    modifier = Modifier
                        .size(100.dp)
                        .padding(8.dp)
                )
            }
            else{
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .padding(8.dp)
                        .background(Color.Gray)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row {
                Button( onClick = {galleryLauncher.launch("image/*") }) {
                    Text("Izaberi iz galerije")
                }
                Spacer(modifier = Modifier.width(16.dp))
                Button(
                    onClick = {
                        if(hasCameraPermision)
                            cameraLauncher.launch(null)
                        else
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    },
                    enabled = hasCameraPermision
                ) {
                    Text("Slikaj")
                }
            }
        }

        CustomTextField(value = ime, onValueChange = { ime = it }, labelText = "Ime:")
        CustomTextField(value = prezime, onValueChange = { prezime = it }, labelText = "Prezime:")
        CustomTextField(value = email, onValueChange = { email = it }, labelText = "Email:", keyboardType = KeyboardType.Email)
        CustomTextField(value = brojTelefona, onValueChange = { brojTelefona = it }, labelText = "Broj telefona:", keyboardType = KeyboardType.Phone)
        CustomTextField(value = username, onValueChange = { username = it }, labelText = "Korisničko ime:")
        CustomTextField(value = password, onValueChange = { password = it }, labelText = "Lozinka:", isPassword = true)

        Button(
            onClick = {
                authViewModel.registerUser(username, email, password, ime, prezime, brojTelefona, profilePicture)
                { success, mes ->
                    if (success) {
                        title = "Obaveštenje"
                        message = "Uspešno ste se registrovali na aplikaciju, sada možete da se prijavite na vaš nalog"
                    }
                    else {
                        password = ""
                        title = "Greška"
                        message = mes ?: "Greška pri registraciji"
                    }
                    showErrorDialog = true
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text("Registruj se")
        }
    }

    if(showErrorDialog){
        AlertDialog(
            onDismissRequest = {},
            title = { Text(title) },
            text = { Text(message) },
            confirmButton =  {
                TextButton(onClick = {
                    showErrorDialog = false
                    message = ""
                    if(title == "Obaveštenje"){
                        onRegisterSuccess()
                    }
                }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    labelText: String,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(labelText) },
        singleLine = true,
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(16.dp))
}