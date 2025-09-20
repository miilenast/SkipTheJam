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
import com.example.skipthejam.ui.screens.AuthChoiceScreen
import com.example.skipthejam.ui.screens.HomeScreen
import com.example.skipthejam.ui.screens.LoginScreen
import com.example.skipthejam.ui.screens.ProfileScreen
import com.example.skipthejam.ui.screens.RegisterScreen
import com.example.skipthejam.ui.theme.SkipTheJamTheme
import com.example.skipthejam.viewmodel.AuthentificationViewModel
import com.google.firebase.auth.FirebaseAuth
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        setContent {
            SkipTheJamTheme {
                val currentUser = FirebaseAuth.getInstance().currentUser
                val startDestination =
                    if(currentUser!=null) "home"
                    else "login"
                AppNavigation()
            }
        }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
    }
}

@Composable
fun AppNavigation() {
    val context = LocalContext.current
    val authViewModel: AuthentificationViewModel = viewModel() // Jednom instanciran

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

    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Screen.Start.route) {
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

                    navController.navigate(Screen.Home.route)
                },
                authViewModel
            )
        }
        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = { navController.navigate(Screen.Start.route) },
                authViewModel
            )
        }
        composable(Screen.Home.route) {
            HomeScreen(goToProfil = { navController.navigate(Screen.Profile.route) })
        }
        composable(Screen.Profile.route) {
            ProfileScreen(
                onLogOut = {
                    navController.navigate(Screen.Start.route) {
                        popUpTo(Screen.Start.route) { inclusive = true }
                    }
                },
                goToHomeScreen = { navController.navigate(Screen.Home.route) },
                authViewModel
            )
        }
    }
}
