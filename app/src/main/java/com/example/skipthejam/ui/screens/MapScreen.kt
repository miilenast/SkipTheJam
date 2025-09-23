package com.example.skipthejam.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.skipthejam.model.EventType
import com.example.skipthejam.viewmodel.LocationViewModel
import com.example.skipthejam.viewmodel.MyLocationsViewModel
import com.google.android.gms.maps.model.*
import com.google.maps.android.compose.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    goToHome:() -> Unit,
    onAddPostClick:() -> Unit,
    onMarkerClick:(String) -> Unit,
    locationViewModel: LocationViewModel = viewModel(),
    myLocationsViewModel: MyLocationsViewModel = viewModel()
){
    val context = LocalContext.current
    val location by locationViewModel.location.collectAsState()
    val filteredLocations by myLocationsViewModel.filteredLocations.collectAsState(emptyList())

    var showFilter by remember { mutableStateOf(false) }
    var isFilterActive by remember { mutableStateOf(false) }
    var showInfo by remember { mutableStateOf(false) }
    var author by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf<EventType?>(null) }
    var time by remember { mutableStateOf("") }
    var radius by remember { mutableStateOf("") }

    val hasLocationPermission =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

    val cameraPositionState = rememberCameraPositionState()

    var hasCentered by remember { mutableStateOf(false) }
    LaunchedEffect(location) {
        location?.let { loc ->
            if(!hasCentered) {
                cameraPositionState.position = CameraPosition.fromLatLngZoom(
                    LatLng(loc.latitude, loc.longitude),
                    15f
                )
                hasCentered=true
            }
            myLocationsViewModel.currentLocation = loc
            myLocationsViewModel.applyFilter()
        }
    }

    if(!hasLocationPermission) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Dozvola za lokaciju nije dobijena")
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ){
                        Text(
                            "Mapa",
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
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    isMyLocationEnabled = true
                ),
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = false,
                    compassEnabled = true
                )
            ) {
                filteredLocations.forEach { loc ->
                    Marker(
                        state = MarkerState(position = LatLng(loc.latitude, loc.longitude)),
                        onClick = {
                            onMarkerClick(loc.id)
                            true
                        }
                    )
                }

                location?.let { currentLocation ->
                    val radiusMeters = radius.toFloatOrNull()?.times(1000)
                    if (radiusMeters != null) {
                        Circle(
                            center = LatLng(currentLocation.latitude, currentLocation.longitude),
                            radius = radiusMeters.toDouble(),
                            fillColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            strokeColor = MaterialTheme.colorScheme.primary,
                            strokeWidth = 2f
                        )
                    }
                }
            }

            FloatingActionButton(
                onClick = { showFilter = true },
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Icon(Icons.Filled.Search, contentDescription = "Filter")
            }

            FloatingActionButton(
                onClick = onAddPostClick,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Post")
            }

            if (isFilterActive) {
                FloatingActionButton(
                    onClick = { showInfo = true },
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 16.dp, bottom = 88.dp)
                ) {
                    Icon(Icons.Filled.Info, contentDescription = "Više informacija")
                }
            }

            if (showFilter) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface.copy(0.95f))
                        .padding(16.dp),
                    Alignment.Center
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .wrapContentHeight(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Filteri", style = MaterialTheme.typography.headlineMedium)
                        Spacer(Modifier.height(16.dp))

                        OutlinedTextField(
                            value = author,
                            onValueChange = { author = it },
                            label = { Text("Korisničko ime autora:") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(16.dp))

                        DropdownMenuExample(selectedType, onSelect = { selectedType = it })
                        Spacer(Modifier.height(16.dp))

                        OutlinedTextField(
                            value = time,
                            onValueChange = { time = it },
                            label = { Text("Vreme od poslednjeg ažuriranja(h):") }
                        )
                        Spacer(Modifier.height(16.dp))

                        OutlinedTextField(
                            value = radius,
                            onValueChange = { radius = it },
                            label = { Text("Radijus(km):") }
                        )
                        Spacer(Modifier.height(24.dp))

                        Row(
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(onClick = {
                                myLocationsViewModel.resetFilters()
                                author = ""
                                selectedType = null
                                time = ""
                                radius = ""
                                showFilter = false
                                isFilterActive = false
                            }) {
                                Text("Poništi filter")
                            }

                            Button(onClick = {
                                val timeMills = time.toLongOrNull()?.times(3600000)
                                val radiusFloat = radius.toFloatOrNull()
                                myLocationsViewModel.apllyUserFilters(
                                    author = author.ifBlank { null },
                                    type = selectedType,
                                    time = timeMills,
                                    radiusKm = radiusFloat
                                )
                                showFilter = false
                                isFilterActive = true
                            }) {
                                Text("Primeni filter")
                            }
                        }
                    }
                }
            }

            if (showInfo) {
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
                            .padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .width(300.dp)
                                .heightIn(max = 400.dp)
                                .background(
                                    MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .padding(16.dp)
                        ) {
                            IconButton(
                                onClick = { showInfo = false },
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Icon(Icons.Filled.Close, contentDescription = "Zatvori")
                            }

                            Text(
                                text = "Filtrirane lokacije",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            Row(modifier = Modifier.fillMaxWidth()) {
                                Text("Korisnik", modifier = Modifier.weight(1f))
                                Text("Tip", modifier = Modifier.weight(1f))
                                Text("Opis", modifier = Modifier.weight(2f))
                            }
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                thickness = DividerDefaults.Thickness,
                                color = DividerDefaults.color
                            )

                            LazyColumn {
                                items(filteredLocations) { location ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                    ) {
                                        Text(location.username, modifier = Modifier.weight(1f))
                                        Text(location.type, modifier = Modifier.weight(1f))
                                        Text(location.description, modifier = Modifier.weight(2f))
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
            }
        }
    }
}

@Composable
private fun DropdownMenuExample(
    selected: EventType?,
    onSelect: (EventType?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Button(onClick = { expanded = true }) {
            Text(selected?.eventName ?: "")
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("") },
                onClick = {
                    onSelect(null)
                    expanded = false
                }
            )
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