package com.notes.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import com.notes.home.presentation.HomeScreen
import com.notes.presentation.splash.SplashScreen

@Composable
internal fun AppNavHost(navController: NavHostController) {
    NavHost(navController, startDestination = NavRoutes.Screens.SPLASH) {
        composable(
            route = NavRoutes.Screens.SPLASH,
            deepLinks = listOf(
                navDeepLink { uriPattern = NavRoutes.DeepLinks.SPLASH }
            )
        ) {
            SplashScreen(navController)
        }
        composable(
            route = NavRoutes.Screens.HOME,
            deepLinks = listOf(
                navDeepLink { uriPattern = NavRoutes.DeepLinks.HOME }
            )
        ) {
            HomeScreen(navController)
        }
    }
}
