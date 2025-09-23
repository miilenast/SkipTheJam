package com.example.skipthejam.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.skipthejam.viewmodel.RangListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopUsersScreen(
    goToHome: () -> Unit,
    rangListViewModel: RangListViewModel = viewModel()
){
    val users by rangListViewModel.topUsers.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ){
                        Text(
                            "10 top korisnika",
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { goToHome() }) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Početna strana",
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
                .padding(padding)
                .padding(16.dp)
        ) {
            // Header row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("", modifier = Modifier.weight(0.5f))
                Text("", modifier = Modifier.weight(1.5f))
                Text("Username", modifier = Modifier.weight(1.5f))
                Text("Poeni", modifier = Modifier.weight(1f))
            }
            HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

            LazyColumn {
                itemsIndexed(users) { index, user ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Text((index+1).toString(), modifier = Modifier.weight(0.5f))

                        if(user.profilnaSlikaURL != null) {
                            Image(
                                painter = rememberAsyncImagePainter(user.profilnaSlikaURL),
                                contentDescription = "Profilna slika",
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .weight(1f)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(MaterialTheme.colorScheme.primary)
                                    .weight(1f)
                            )
                        }

                        Text(user.username, modifier = Modifier.weight(2f))
                        Text(user.poeni.toString(), modifier = Modifier.weight(1f))
                    }

                    HorizontalDivider(
                        Modifier,
                        DividerDefaults.Thickness,
                        DividerDefaults.color
                    )
                }
            }
        }
    }
}