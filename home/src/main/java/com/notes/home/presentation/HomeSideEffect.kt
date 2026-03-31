package com.notes.home.presentation

internal sealed interface HomeSideEffect {
    data class ShowSnackbar(val message: String) : HomeSideEffect
    data class ShowError(val errorMessage: String) : HomeSideEffect
    data class NavigateToDetail(val topicId: Int) : HomeSideEffect
}

