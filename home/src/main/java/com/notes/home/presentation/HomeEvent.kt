package com.notes.home.presentation

import com.notes.core.model.Topic

internal sealed interface HomeEvent {
    data class AddTopic(val title: String, val description: String) : HomeEvent
    data class DeleteTopic(val topic: Topic) : HomeEvent
    data class Search(val query: String) : HomeEvent
}
