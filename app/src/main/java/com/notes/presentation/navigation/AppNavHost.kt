package com.notes.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import androidx.navigation.NavType
import com.notes.presentation.home.HomeScreen
import com.notes.presentation.splash.SplashScreen
import com.notes.utils.NotesConstants.EMPTY_VALUE

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
            arguments = listOf(
                navArgument(NavRoutes.Args.MSG) {
                    type = NavType.StringType
                    defaultValue = EMPTY_VALUE
                }
            ),
            deepLinks = listOf(
                navDeepLink { uriPattern = NavRoutes.DeepLinks.HOME }
            )
        ) { backStackEntry ->
            val message = backStackEntry.arguments?.getString(NavRoutes.Args.MSG).orEmpty()
            HomeScreen(message)
        }
    }
}
