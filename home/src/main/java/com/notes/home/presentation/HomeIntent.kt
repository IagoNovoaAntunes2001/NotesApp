package com.notes.home.presentation

import com.notes.core.model.Topic

internal sealed interface HomeIntent {
    data object LoadTopics : HomeIntent
    data object Refresh : HomeIntent   // sync manual (pull-to-refresh)
    data class AddTopic(val title: String, val description: String) : HomeIntent
    data class DeleteTopic(val topic: Topic) : HomeIntent
    data class Search(val query: String) : HomeIntent
    data class NavigateToDetail(val topic: Topic) : HomeIntent
}
