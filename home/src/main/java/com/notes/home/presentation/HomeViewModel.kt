package com.notes.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notes.home.domain.entities.Topic
import com.notes.home.domain.usecases.AddTopicUseCase
import com.notes.home.domain.usecases.DeleteTopicUseCase
import com.notes.home.domain.usecases.GetTopicsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val getTopicsUseCase: GetTopicsUseCase,
    private val addTopicUseCase: AddTopicUseCase,
    private val deleteTopicUseCase: DeleteTopicUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun loadTopics() {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                _uiState.update {
                    it.copy(isLoading = false, topics = getTopicsUseCase())
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun onEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.AddTopic -> addTopic(event.title, event.description)
            is HomeEvent.DeleteTopic -> deleteTopic(event.topic)
            is HomeEvent.Search -> _uiState.update { it.copy(searchQuery = event.query) }
        }
    }

    private fun addTopic(title: String, description: String) {
        viewModelScope.launch {
            try {
                addTopicUseCase(title, description)
                loadTopics()
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message) }
            }
        }
    }

    private fun deleteTopic(topic: Topic) {
        viewModelScope.launch {
            try {
                deleteTopicUseCase(topic)
                loadTopics()
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message) }
            }
        }
    }
}
