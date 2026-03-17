package com.notes.home.presentation

import com.notes.home.domain.entities.Topic

sealed interface HomeIntent {
    data object LoadTopics : HomeIntent
    data class AddTopic(val title: String, val description: String) : HomeIntent
    data class DeleteTopic(val topic: Topic) : HomeIntent
    data class Search(val query: String) : HomeIntent
}

