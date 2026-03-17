package com.notes.home.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notes.home.domain.entities.Topic
import com.notes.home.domain.usecases.AddTopicUseCase
import com.notes.home.domain.usecases.DeleteTopicUseCase
import com.notes.home.domain.usecases.GetTopicsUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val getTopicsUseCase: GetTopicsUseCase,
    private val addTopicUseCase: AddTopicUseCase,
    private val deleteTopicUseCase: DeleteTopicUseCase,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    companion object {
        private const val KEY_SEARCH_QUERY = "search_query"
    }

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // SavedStateHandle salva searchQuery (sobrevive process death)
    var searchQuery: String
        get() = savedStateHandle.get<String>(KEY_SEARCH_QUERY) ?: ""
        set(value) {
            savedStateHandle[KEY_SEARCH_QUERY] = value
            reduce { copy(searchQuery = value) }
        }

    private val _sideEffect = Channel<HomeSideEffect>()
    val sideEffect = _sideEffect.receiveAsFlow()

    fun processIntent(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.LoadTopics -> loadTopics()
            is HomeIntent.AddTopic -> addTopic(intent.title, intent.description)
            is HomeIntent.DeleteTopic -> deleteTopic(intent.topic)
            is HomeIntent.Search -> {
                searchQuery = intent.query
            }
        }
    }

    private fun loadTopics() {
        reduce { copy(isLoading = true) }
        viewModelScope.launch {
            try {
                val topics = getTopicsUseCase()
                reduce { copy(isLoading = false, topics = topics) }
            } catch (e: Exception) {
                reduce { copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    private fun addTopic(title: String, description: String) {
        viewModelScope.launch {
            try {
                addTopicUseCase(title, description)
                emitSideEffect(HomeSideEffect.ShowSnackbar("Tópico adicionado com sucesso!"))
                processIntent(HomeIntent.LoadTopics)
            } catch (e: Exception) {
                emitSideEffect(HomeSideEffect.ShowError(e.message ?: "Erro ao adicionar tópico"))
                reduce { copy(errorMessage = e.message) }
            }
        }
    }

    private fun deleteTopic(topic: Topic) {
        viewModelScope.launch {
            try {
                deleteTopicUseCase(topic)
                emitSideEffect(HomeSideEffect.ShowSnackbar("Tópico removido!"))
                processIntent(HomeIntent.LoadTopics)
            } catch (e: Exception) {
                emitSideEffect(HomeSideEffect.ShowError(e.message ?: "Erro ao remover tópico"))
                reduce { copy(errorMessage = e.message) }
            }
        }
    }

    private fun reduce(reducer: HomeUiState.() -> HomeUiState) {
        _uiState.update { it.reducer() }
    }

    private fun emitSideEffect(effect: HomeSideEffect) {
        viewModelScope.launch {
            _sideEffect.send(effect)
        }
    }
}
