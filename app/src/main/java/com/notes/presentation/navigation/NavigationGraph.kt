package com.notes.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.notes.detail.navigation.createDetailRoute
import com.notes.detail.navigation.detailNavGraph
import com.notes.home.navigation.HOME_ROUTE
import com.notes.home.navigation.homeNavGraph

@Composable
internal fun NavigationGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = HOME_ROUTE
    ) {
        homeNavGraph(
            onNavigateToDetail = { topicId ->
                navController.navigate(createDetailRoute(topicId))
            }
        )
        detailNavGraph(
            onNavigateBack = { navController.popBackStack() }
        )
    }
}
