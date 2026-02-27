package com.notes.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notes.home.presentation.model.Topic
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadTopics()
    }

    private fun loadTopics() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            delay(1_000)

            val simulatedTopics = listOf(
                Topic(id = 1, title = "Kotlin Coroutines", description = "Asynchronous programming with coroutines and flows."),
                Topic(id = 2, title = "Jetpack Compose", description = "Building declarative UIs with Compose."),
                Topic(id = 3, title = "Clean Architecture", description = "Separating concerns with domain, data and presentation layers."),
                Topic(id = 4, title = "StateFlow & SharedFlow", description = "Reactive state management in ViewModels."),
                Topic(id = 5, title = "Dependency Injection", description = "Managing dependencies with Hilt or Koin.")
            )

            _uiState.update {
                it.copy(
                    isLoading = false,
                    topics = simulatedTopics
                )
            }
        }
    }
}
