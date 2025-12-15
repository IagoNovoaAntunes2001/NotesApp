package com.notes.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.notes.presentation.splash.SplashScreen

@Composable
internal fun AppNavHost(navController: NavHostController) {
    NavHost(navController, startDestination = NavRoutes.SPLASH) {

        composable(NavRoutes.SPLASH) {
            SplashScreen(onNavigateHome = {})
        }
    }
}