package com.example.skipthejam

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.skipthejam.ui.navigation.Screen
import com.example.skipthejam.ui.screens.AuthChoiceScreen
import com.example.skipthejam.ui.screens.HomeScreen
import com.example.skipthejam.ui.screens.LoginScreen
import com.example.skipthejam.ui.screens.RegisterScreen
import com.example.skipthejam.ui.theme.SkipTheJamTheme
import com.example.skipthejam.viewmodel.AuthentificationViewModel

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
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Screen.Start.route) {
        composable(Screen.Start.route) {
            AuthChoiceScreen (
                onLoginClick = {navController.navigate(Screen.Login.route)},
                onRegisterClick = {navController.navigate(Screen.Register.route)}
            )
        }
        composable(Screen.Login.route) {
            val authViewModel: AuthentificationViewModel = viewModel()
            LoginScreen(
                onLoginSuccess = { navController.navigate(Screen.Home.route) },
                authViewModel
            )
        }
        composable(Screen.Register.route) {
            val authViewModel: AuthentificationViewModel = viewModel()
            RegisterScreen(
                onRegisterSuccess = { navController.navigate(Screen.Home.route) },
                authViewModel
            )
        }
        composable(Screen.Home.route) {
            val authViewModel: AuthentificationViewModel = viewModel()
            HomeScreen(
                onLogOut = {
                    navController.navigate(Screen.Start.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                authViewModel
            )
        }
    }
}