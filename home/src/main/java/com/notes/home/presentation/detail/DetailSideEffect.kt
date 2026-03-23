package com.notes.home.presentation.detail

sealed interface DetailSideEffect {
    data object NavigateBack : DetailSideEffect
}

