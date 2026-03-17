package com.notes.home.presentation

sealed interface HomeSideEffect {
    data class ShowSnackbar(val message: String) : HomeSideEffect
    data class ShowError(val errorMessage: String) : HomeSideEffect
}

