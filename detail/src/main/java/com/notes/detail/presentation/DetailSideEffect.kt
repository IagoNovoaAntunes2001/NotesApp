package com.notes.detail.presentation

internal sealed interface DetailSideEffect {
    data object NavigateBack : DetailSideEffect
    data class ShowSnackbar(val message: String) : DetailSideEffect
}
