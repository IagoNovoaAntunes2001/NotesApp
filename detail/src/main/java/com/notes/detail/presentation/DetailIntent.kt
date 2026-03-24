package com.notes.detail.presentation

sealed interface DetailIntent {
    data class LoadTopic(val id: Int) : DetailIntent
    data object GoBack : DetailIntent
}
