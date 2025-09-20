package com.example.skipthejam.ui.screens

import android.app.Activity
import android.graphics.drawable.Icon
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.skipthejam.viewmodel.AuthentificationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    goToProfil: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    BackHandler { activity?.moveTaskToBack(true) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Početna strana") },
                actions = {
                    IconButton(onClick = { goToProfil() }) {
                        Icon(
                            imageVector = Icons.Filled.AccountBox,
                            contentDescription = "Korisnički profil",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "Home Page")
        }
    }
}