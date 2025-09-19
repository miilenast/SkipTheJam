package com.example.skipthejam.ui.navigation

sealed class Screen(val route: String) {
    object Start: Screen("start")
    object Login: Screen("login")
    object Register: Screen("register")
    object Home: Screen("home")
}