package com.notes.home.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.notes.home.presentation.HomeScreen

const val HOME_ROUTE = "home"

fun NavGraphBuilder.homeNavGraph(onNavigateToDetail: (Int) -> Unit) {
    composable(route = HOME_ROUTE) {
        HomeScreen(onNavigateToDetail = onNavigateToDetail)
    }
}
