package com.notes.home.presentation.detail

import com.notes.home.domain.entities.Topic

data class DetailUiState(
    val isLoading: Boolean = false,
    val topic: Topic? = null,
    val errorMessage: String? = null
)

