package com.example.skipthejam.ui.screens

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    goToProfil: () -> Unit,
    goToMap: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    BackHandler { activity?.moveTaskToBack(true) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ){
                        Text(
                            "Početna strana",
                            color = MaterialTheme.colorScheme.primary
                        )
                     }
                },
                navigationIcon = {
                    IconButton(onClick = { goToMap() }) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Map",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { goToProfil() }) {
                        Icon(
                            imageVector = Icons.Filled.AccountBox,
                            contentDescription = "Korisnički profil",
                            tint = MaterialTheme.colorScheme.primary
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
            ) {
            Text(
                text = "Home Page",
                modifier = Modifier.align(Alignment.Center)
            )

            Button(
                onClick = { goToMap() },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom=32.dp)
            ){
                Text("Mapa")
            }
        }
    }
}