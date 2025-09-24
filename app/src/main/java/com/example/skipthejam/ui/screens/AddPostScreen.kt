package com.example.skipthejam.ui.screens

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.skipthejam.model.EventType
import com.example.skipthejam.viewmodel.LocationViewModel
import com.example.skipthejam.viewmodel.MyLocationsViewModel
import android.Manifest
import android.content.pm.PackageManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPostScreen(
    onSaveClick: () -> Unit,
    goToMap: () -> Unit,
    myLocationsViewModel: MyLocationsViewModel = viewModel(),
    locationViewModel: LocationViewModel = viewModel()
){
    val context = LocalContext.current
    var description by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(EventType.TRAFFIC_JAM) }
    var selectedImage: Uri? by remember { mutableStateOf(null) }
    var tempImageUri by remember { mutableStateOf<Uri?>(null) }

    var showDialog by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    var hasCameraPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        selectedImage =
            if(success) tempImageUri
            else null
    }

    fun createImageUri(context: Context): Uri? {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "post_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        }
        return context.contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        )
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        hasCameraPermission = isGranted
        if (isGranted) {
            val uri = createImageUri(context)
            if (uri != null) {
                tempImageUri = uri
                cameraLauncher.launch(uri)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ){
                        Text(
                            "Dodavanje događaja",
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { goToMap() }) {
                        Icon(imageVector = Icons.Default.LocationOn,
                            contentDescription = "Map",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (selectedImage != null) {
                Image(
                    painter = rememberAsyncImagePainter(selectedImage),
                    contentDescription = "Slika posta",
                    modifier = Modifier
                        .size(200.dp)
                        .padding(8.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .padding(8.dp)
                        .background(Color.Gray),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Nema slike", color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if(hasCameraPermission) {
                        val uri = createImageUri(context)
                        if (uri != null) {
                            tempImageUri = uri
                            cameraLauncher.launch(uri)
                        }
                    }
                    else cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                },
                enabled = hasCameraPermission
            ) {
                Text("Slikaj")
            }

            Spacer(modifier = Modifier.height(24.dp))

            DropdownMenuExample(
                selected = selectedType,
                onSelect = { selectedType = it }
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Opis") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {},
                singleLine = false,
                maxLines = 4
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = { goToMap() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Otkaži")
                }

                Button(
                    onClick = {
                        Log.d("AddPostScreen", "Sačuvaj dugme kliknuto")
                        val currentLocation = locationViewModel.location.value
                        if(currentLocation == null){
                            showDialog = true
                            title = "Greška"
                            message = "Lokacija je nepoznata. Pokušajte ponovo."
                        } else {
                            Log.d("AddPostScreen", "Poziv saveLocation sa loc: $currentLocation")
                            myLocationsViewModel.saveLocation(
                                type = selectedType,
                                image = selectedImage,
                                description = description,
                                latitude = currentLocation.latitude,
                                longitude = currentLocation.longitude
                            ) { success, msg ->
                                Log.d("AddPostScreen", "saveLocation result: $success, $msg")
                                if (success) {
                                    title = "Obaveštenje"
                                    message = msg ?: "Uspešno ste dodali lokaciju"
                                    showDialog = true
                                }
                                else {
                                    title = "Greška"
                                    message = msg ?: "Greška prilikom dodavanja lokacije"
                                    showDialog = true
                                }
                            }
                        }
                    },
                    enabled = description.isNotBlank(),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Sačuvaj")
                }
            }

            if (showDialog) {
                AlertDialog(
                    onDismissRequest = {},
                    title = { Text(title) },
                    text = { Text(message) },
                    confirmButton = {
                        TextButton(onClick = {
                            showDialog = false
                            message = ""
                            if (title == "Obaveštenje") {
                                onSaveClick()
                            }
                        }) {
                            Text("OK")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun DropdownMenuExample(
    selected: EventType,
    onSelect: (EventType) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Button(onClick = { expanded = true }) {
            Text(selected.eventName)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            EventType.entries.forEach { type ->
                DropdownMenuItem(
                    text = { Text(type.eventName) },
                    onClick = {
                        onSelect(type)
                        expanded = false
                    }
                )
            }
        }
    }
}
