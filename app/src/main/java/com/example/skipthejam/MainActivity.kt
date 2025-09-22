package com.example.skipthejam

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.skipthejam.ui.navigation.Screen
import com.example.skipthejam.ui.screens.*
import com.example.skipthejam.ui.theme.SkipTheJamTheme
import com.example.skipthejam.viewmodel.AuthentificationViewModel
import com.google.firebase.auth.FirebaseAuth
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.example.skipthejam.viewmodel.LocationViewModel
import com.example.skipthejam.viewmodel.MyLocationsViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SkipTheJamTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val context = LocalContext.current
    val navController = rememberNavController()
    val authViewModel: AuthentificationViewModel = viewModel()
    val locationViewModel: LocationViewModel = viewModel()
    val myLocationsViewModel: MyLocationsViewModel = viewModel()

    val currentUser = FirebaseAuth.getInstance().currentUser
    val startDestination =
        if(currentUser!=null) Screen.Home.route
        else Screen.Start.route

    val notificationPermissionLauncher =
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { }
        } else null

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            if(ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)!= PackageManager.PERMISSION_GRANTED)
                notificationPermissionLauncher?.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Screen.Start.route) {
            AuthChoiceScreen (
                onLoginClick = { navController.navigate(Screen.Login.route) },
                onRegisterClick = { navController.navigate(Screen.Register.route) }
            )
        }
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    if(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED)
                        locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)

                    navController.navigate(Screen.Home.route){
                        popUpTo(0) {inclusive = true}
                        launchSingleTop = true
                    }
                },
                authViewModel
            )
        }
        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = { navController.navigate(Screen.Start.route) {
                    popUpTo(Screen.Start.route) { inclusive = true }
                } },
                authViewModel
            )
        }
        composable(Screen.Home.route) {
            HomeScreen(
                goToProfil = { navController.navigate(Screen.Profile.route) },
                goToMap = { navController.navigate(Screen.Map.route) }
            )
        }
        composable(Screen.Profile.route) {
            ProfileScreen(
                onLogOut = {
                    navController.navigate(Screen.Start.route) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                goToHomeScreen = { navController.navigate(Screen.Home.route) },
                authViewModel
            )
        }
        composable(Screen.Map.route){
            MapScreen(
                onFilterClick = {},
                onAddPostClick = {navController.navigate(Screen.AddPost.route)},
                onMarkerClick = {navController.navigate(Screen.Post.route)},
                locationViewModel,
                myLocationsViewModel
            )
        }
        composable(Screen.AddPost.route) {
            AddPostScreen(
                onSaveClick = { navController.popBackStack() },
                onCancelClick = { navController.navigate(Screen.Map.route)},
                myLocationsViewModel,
                locationViewModel
            )
        }
    }
}
