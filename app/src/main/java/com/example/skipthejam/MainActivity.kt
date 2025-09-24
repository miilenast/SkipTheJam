package com.example.skipthejam

import com.example.skipthejam.service.LocationUpdateWorker
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.skipthejam.viewmodel.LocationViewModel
import com.example.skipthejam.viewmodel.MyLocationsViewModel
import com.example.skipthejam.viewmodel.PostViewModel
import com.example.skipthejam.viewmodel.RangListViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        startLocationUpdates()
        setContent {
            SkipTheJamTheme {
                AppNavigation()
            }
        }
    }

    fun startLocationUpdates() {
        val workRequest = PeriodicWorkRequestBuilder<LocationUpdateWorker>(
            5,
            TimeUnit.MINUTES
        ).build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "locationUpdateWorker",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}

@Composable
fun AppNavigation() {
    val context = LocalContext.current
    val navController = rememberNavController()
    val authViewModel: AuthentificationViewModel = viewModel()
    val locationViewModel: LocationViewModel = viewModel()
    val myLocationsViewModel: MyLocationsViewModel = viewModel()
    val rangListViewModel: RangListViewModel = viewModel()
    val postViewModel: PostViewModel = viewModel()

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

                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                        ContextCompat.checkSelfPermission(context,Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
                        notificationPermissionLauncher?.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }

                    navController.navigate(Screen.Home.route){
                        popUpTo(0) {inclusive = true}
                        launchSingleTop = true
                    }

                    FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val token = task.result
                            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@addOnCompleteListener

                            FirebaseFirestore.getInstance().collection("users").document(uid)
                                .update("fcmToken", token)
                        }
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
                goToProfile = { navController.navigate(Screen.Profile.route) },
                goToMap = { navController.navigate(Screen.Map.route) },
                goToTopUsers = { navController.navigate(Screen.RangList.route) }
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
                goToHome = {navController.navigate(Screen.Home.route){
                    popUpTo(Screen.Map.route) {inclusive = true}
                    launchSingleTop = true
                } },
                onAddPostClick = {navController.navigate(Screen.AddPost.route)},
                onMarkerClick = { locationId ->
                    navController.navigate(Screen.Post.createRoute(locationId))
                },
                locationViewModel,
                myLocationsViewModel
            )
        }
        composable(
            route = Screen.Post.route,
            arguments = listOf(navArgument("locationId") { type = NavType.StringType })
        ) { backStackEntry ->
            val locationId = backStackEntry.arguments?.getString("locationId") ?: return@composable
            PostScreen(locationId = locationId,
                postViewModel = postViewModel,
                locationViewModel = locationViewModel,
                goToMap = {navController.navigate(Screen.Map.route){
                    popUpTo(Screen.Post.route) {inclusive = true}
                } }
            )
        }
        composable(Screen.AddPost.route) {
            AddPostScreen(
                onSaveClick = { navController.popBackStack() },
                goToMap = { navController.navigate(Screen.Map.route){
                    popUpTo(Screen.AddPost.route) {inclusive = true}
                    launchSingleTop = true
                } },
                myLocationsViewModel,
                locationViewModel
            )
        }
        composable(Screen.RangList.route) {
            TopUsersScreen(
                goToHome = {navController.navigate(Screen.Home.route){
                    popUpTo(Screen.Map.route) {inclusive = true}
                    launchSingleTop = true
                } },
                rangListViewModel
            )
        }

    }
}
