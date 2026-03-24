package com.notes.detail.presentation

sealed interface DetailSideEffect {
    data object NavigateBack : DetailSideEffect
}
