package com.notes.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.notes.home.presentation.HomeScreen

@Composable
internal fun NavigationGraph(startDestination: String = Screen.Home.route) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(route = Screen.Home.route) {
            HomeScreen(navController)
        }
    }
}
