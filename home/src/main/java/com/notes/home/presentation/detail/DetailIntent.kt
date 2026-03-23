package com.notes.home.presentation.detail

sealed interface DetailIntent {
    data class LoadTopic(val id: Int) : DetailIntent
    data object GoBack : DetailIntent
}

