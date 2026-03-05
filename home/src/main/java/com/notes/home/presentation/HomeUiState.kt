package com.notes.home.presentation

import com.notes.home.domain.entities.Topic

data class HomeUiState(
    val isLoading: Boolean = false,
    val topics: List<Topic> = emptyList(),
    val errorMessage: String? = null
)
