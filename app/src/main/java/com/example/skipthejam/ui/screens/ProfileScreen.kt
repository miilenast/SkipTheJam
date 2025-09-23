package com.example.skipthejam.ui.screens

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.skipthejam.viewmodel.AuthentificationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onLogOut: () -> Unit,
    goToHomeScreen: () -> Unit,
    authViewModel: AuthentificationViewModel
) {
    var showDialog by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    val currentUser by authViewModel.currentUserUser
    var showLogOutDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current

    val notificationPermissionLauncher =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { granted ->
                if (granted) {
                    message =
                        "Od sada ćete primati notifikacije o gužvama u saobraćaju u Vašoj neposrednoj blizini"
                    title = "Notifikacije"
                    showDialog = true
                }
            }
        } else null

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ){
                        Text(
                            "Profil",
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { goToHomeScreen() }) {
                        Icon(imageVector = Icons.Default.Home,
                            contentDescription = "Home",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        showLogOutDialog = true
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Logout",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(padding),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            currentUser?.let { user ->
                Spacer(modifier = Modifier.height(48.dp))

                user.profilnaSlikaURL?.let { url ->
                    Image(
                        painter = rememberAsyncImagePainter(url),
                        contentDescription = "Profilna slika",
                        modifier = Modifier
                            .size(120.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )
                } ?: Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.primary)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Korisničko ime: ${user.username}",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Ime: ${user.ime}", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Prezime: ${user.prezime}", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Broj telefona: ${user.brojTelefona}", style = MaterialTheme.typography.bodyMedium)

                Spacer(modifier = Modifier.height(48.dp))

                Text(
                    text = "Broj poena: ${user.poeni}",
                    style = MaterialTheme.typography.titleMedium
                )

            }

            Spacer(modifier = Modifier.weight(1f))
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = {},
                title = { Text(title) },
                text = { Text(message) },
                confirmButton = {
                    TextButton(onClick = {
                        if (title == "Obaveštenje") {
                            onLogOut()
                        }
                        showDialog = false
                        message = ""
                        title = ""
                    }) {
                        Text("OK")
                    }
                }
            )
        }

        if(showLogOutDialog) {
            AlertDialog(
                onDismissRequest = {},
                title = { Text("Odjavljivanje") },
                text = { Text("Da li ste sigurni da želite da se odjavite?") },
                confirmButton = {
                    TextButton(onClick = {
                        showLogOutDialog = false
                        authViewModel.logoutUser { success, messagee ->
                            if (success) {
                                message = "Odjavili ste se sa profila"
                                title = "Obaveštenje"
                            } else {
                                message = messagee ?: "Nije moguće odjaviti se trenutno"
                                title = "Greška"
                            }
                            showDialog = true
                        }
                    }) {
                        Text("Odjavi me")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {showLogOutDialog = false}) {
                        Text("Odustani")
                    }
                }
            )
        }
    }
}
