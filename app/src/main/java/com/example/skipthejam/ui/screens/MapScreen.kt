package com.example.skipthejam.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.skipthejam.viewmodel.LocationViewModel
import com.example.skipthejam.viewmodel.MyLocationsViewModel
import com.google.android.gms.maps.model.*
import com.google.maps.android.compose.*

@Composable
fun MapScreen(
    onFilterClick: () -> Unit,
    onAddPostClick:() -> Unit,
    onMarkerClick:(String) -> Unit,
    locationViewModel: LocationViewModel = viewModel(),
    myLocationsViewModel: MyLocationsViewModel = viewModel()
){
    val context = LocalContext.current
    val location by locationViewModel.location.collectAsState()
    val filteredLocations by myLocationsViewModel.filteredLocations.collectAsState(emptyList())

    val hasLocationPermission =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

    val cameraPositionState = rememberCameraPositionState()

    LaunchedEffect(location) {
        location?.let { loc ->
            cameraPositionState.position = CameraPosition.fromLatLngZoom(
                LatLng(loc.latitude, loc.longitude),
                15f
            )
        }
    }

//    val cameraPositionState = rememberCameraPositionState {
//        position = CameraPosition.fromLatLngZoom(LatLng(43.3209, 21.8958), 15f)
//    }

//    LaunchedEffect(location) {
//        location?.let { loc ->
//            cameraPositionState.position = CameraPosition.fromLatLngZoom(
//                LatLng(loc.latitude, loc.longitude),
//                15f
//            )
//        }
//    }

    if(!hasLocationPermission) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Dozvola za lokaciju nije dobijena")
        }
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
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
//            location?.let{ loc ->
//                Marker(
//                    state = MarkerState(position = LatLng(loc.latitude, loc.longitude)),
//
//                )
//            }
        }

        FloatingActionButton(
            onClick = onFilterClick,
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
    }
}