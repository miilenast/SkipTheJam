package com.example.skipthejam.ui.navigation

sealed class Screen(val route: String) {
    object Start: Screen("start")
    object Login: Screen("login")
    object Register: Screen("register")
    object Home: Screen("home")
    object Profile: Screen("profile")
    object Map: Screen("map")
    object AddPost: Screen("addpost")
    object Post: Screen("post/{locationId}"){
        fun createRoute(locationId: String) = "post/$locationId"
    }
    object RangList: Screen("ranglist")
}