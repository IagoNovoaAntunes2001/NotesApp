package com.notes.presentation.navigation


sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Detail : Screen("detail/{topicId}") {
        const val ARG_TOPIC_ID = "topicId"
        fun createRoute(topicId: Int) = "detail/$topicId"
    }
}
