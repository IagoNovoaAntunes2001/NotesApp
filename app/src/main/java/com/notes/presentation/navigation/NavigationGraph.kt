package com.notes.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.notes.home.presentation.HomeScreen
import com.notes.home.presentation.detail.DetailScreen

@Composable
internal fun NavigationGraph(startDestination: String = Screen.Home.route) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(route = Screen.Home.route) {
            HomeScreen(
                onNavigateToDetail = { topicId ->
                    navController.navigate(Screen.Detail.createRoute(topicId))
                }
            )
        }
        composable(
            route = Screen.Detail.route,
            arguments = listOf(navArgument(Screen.Detail.ARG_TOPIC_ID) { type = NavType.IntType })
        ) { backStackEntry ->
            val topicId = backStackEntry.arguments?.getInt(Screen.Detail.ARG_TOPIC_ID) ?: return@composable
            DetailScreen(
                topicId = topicId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
