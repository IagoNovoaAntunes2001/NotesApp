package com.notes.detail.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.notes.detail.presentation.DetailScreen

const val ARG_TOPIC_ID = "topicId"
const val DETAIL_ROUTE = "detail/{$ARG_TOPIC_ID}"

fun createDetailRoute(topicId: Int) = "detail/$topicId"

fun NavGraphBuilder.detailNavGraph(onNavigateBack: () -> Unit) {
    composable(
        route = DETAIL_ROUTE,
        arguments = listOf(navArgument(ARG_TOPIC_ID) { type = NavType.IntType })
    ) { backStackEntry ->
        val topicId = backStackEntry.arguments?.getInt(ARG_TOPIC_ID) ?: return@composable
        DetailScreen(topicId = topicId, onNavigateBack = onNavigateBack)
    }
}
