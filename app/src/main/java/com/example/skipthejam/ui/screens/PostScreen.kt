package com.example.skipthejam.ui.screens

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil.compose.rememberAsyncImagePainter
import com.example.skipthejam.viewmodel.LocationViewModel
import com.example.skipthejam.viewmodel.PostViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostScreen(
    locationId: String,
    postViewModel: PostViewModel,
    locationViewModel: LocationViewModel,
    goToMap: () -> Unit
){
    LaunchedEffect(locationId){
        postViewModel.selectLocation(locationId)
    }

    val post = postViewModel.selectedLocation.collectAsState().value
    val comments = postViewModel.comment.collectAsState().value
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showAddCommentDialog by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }

    val currLocation = locationViewModel.location.collectAsState().value
    var showButtons by remember { mutableStateOf(false) }
    LaunchedEffect(currLocation, post) {
        if (currLocation != null && post != null) {
            val result = FloatArray(1)
            Location.distanceBetween(
                currLocation.latitude, currLocation.longitude,
                post.latitude, post.longitude,
                result
            )
            showButtons = result[0] <= 100
        } else {
            showButtons = false
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
                            "Informacije o lokaciji",
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { goToMap() }) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Mapa",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        }
    ){ padding ->
        post?.let { loc ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ){
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val timestamp = loc.timestamp
                    val date = Date(timestamp)
                    val formattedDate = SimpleDateFormat("dd.MM.yyyy, HH:mm", Locale.getDefault()).format(date)
                    Text(text = "@${loc.username}  $formattedDate",style = MaterialTheme.typography.titleMedium)
                }
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!loc.imageUrl.isNullOrEmpty()) {
                        Image(
                            painter = rememberAsyncImagePainter(loc.imageUrl),
                            contentDescription = "Slika lokacije",
                            modifier = Modifier
                                .size(120.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    }

                    Spacer(Modifier.width(32.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(loc.type, style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Opis: ${loc.description}", style = MaterialTheme.typography.bodyMedium)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if(showButtons) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(onClick = { showDeleteDialog = true }) {
                            Text("Obriši post")
                        }

                        Button(onClick = { showAddCommentDialog = true }) {
                            Text("Dodaj komentar")
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

                Spacer(modifier = Modifier.height(16.dp))
                Text("Komentari:", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn {
                    items(comments) { comment ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            val timestamp = comment.timestamp
                            val date = Date(timestamp)
                            val formattedDate = SimpleDateFormat("dd.MM.yyyy, HH:mm", Locale.getDefault()).format(date)
                            Text(
                                text = "@"+comment.username+"  "+formattedDate,
                                style = MaterialTheme.typography.titleSmall
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (!comment.imageUrl.isNullOrEmpty()) {
                                    Image(
                                        painter = rememberAsyncImagePainter(comment.imageUrl),
                                        contentDescription = "Slika komentara",
                                        modifier = Modifier
                                            .size(80.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(comment.description, style = MaterialTheme.typography.bodySmall)
                            }
                        }

                        HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
                    }
                }
            }

            if(showAddCommentDialog){
                AddCommentDialog(postViewModel,
                    onCancel = { showAddCommentDialog = false }
                )
            }

            if(showDeleteDialog){
                AlertDialog(
                    onDismissRequest = {},
                    title = { Text("Trajno brisanje lokacije") },
                    text = { Text("Ponašajte se savesno i obrišite lokaciju samo ako problem na putu svarno više ne postoji!") },
                    dismissButton = {
                        TextButton(onClick = { showDeleteDialog = false }) { Text("Otkaži") }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                postViewModel.deleteLocation { success, mes ->
                                    if(success) showDeleteDialog = false
                                    else{
                                        showDeleteDialog = false
                                        message = mes ?: "Greška pri brisanju lokacije"
                                        showError = true
                                    }
                                }
                            }
                        ) { Text("Obriši") }
                    }
                )
            }
            if(showError){
                AlertDialog(
                    onDismissRequest = {},
                    title = { Text("Greška") },
                    text = { Text(message) },
                    confirmButton = {
                        TextButton(onClick = { showError = false }) {
                            Text("OK")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun AddCommentDialog(
    postViewModel: PostViewModel,
    onCancel: () -> Unit
){
    var description by remember { mutableStateOf("") }
    var selectedImage by remember { mutableStateOf<Uri?>(null) }
    var tempImageUri by remember { mutableStateOf<Uri?>(null) }

    val context = LocalContext.current

    var showDialog by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        selectedImage =
            if(success) tempImageUri
            else null
    }

    fun createImageUri(context: Context): Uri? {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "comment_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        }
        return context.contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        )
    }

    var hasCameraPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
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

    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.7f)
                .background(MaterialTheme.colorScheme.surface)
                .border(width = 1.dp, color = MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(8.dp))
                .padding(16.dp)
        ){
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top,
                modifier = Modifier.fillMaxSize()
            ){
                IconButton(
                    onClick = { onCancel() },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Icon(Icons.Filled.Close, contentDescription = "Zatvori")
                }

                Text("Dodaj komentar", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(16.dp))

                if (selectedImage != null) {
                    Image(
                        painter = rememberAsyncImagePainter(selectedImage),
                        contentDescription = "Slika komentara",
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
                Spacer(modifier = Modifier.height(16.dp))

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
                Spacer(modifier = Modifier.height(16.dp))

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

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = { onCancel() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Otkaži")
                    }

                    Button(
                        onClick = {
                            postViewModel.addComment(description,selectedImage) { success, msg ->
                                title = if (success) "Obaveštenje" else "Greška"
                                message = msg ?: ""
                                showDialog = true
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
                                onCancel()
                            }) {
                                Text("OK")
                            }
                        }
                    )
                }
            }
        }
    }
}