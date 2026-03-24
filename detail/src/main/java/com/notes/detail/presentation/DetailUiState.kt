package com.notes.detail.presentation

import com.notes.core.model.Topic

data class DetailUiState(
    val isLoading: Boolean = false,
    val topic: Topic? = null,
    val errorMessage: String? = null
)

