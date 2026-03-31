package com.notes.home.presentation

import com.notes.core.model.Topic

internal data class HomeUiState(
    val isLoading: Boolean = false,
    val topics: List<Topic> = emptyList(),
    val searchQuery: String = "",
    val errorMessage: String? = null
)
