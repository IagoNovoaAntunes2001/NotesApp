package com.notes.detail.presentation

internal sealed interface DetailIntent {
    data class LoadTopic(val id: Int) : DetailIntent
    data object GoBack : DetailIntent
}
