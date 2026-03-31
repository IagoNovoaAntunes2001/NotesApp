package com.notes.detail.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notes.core.data.usecase.GetTopicByIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class DetailViewModel @Inject constructor(
    private val getTopicByIdUseCase: GetTopicByIdUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    private val _sideEffect = Channel<DetailSideEffect>()
    val sideEffect = _sideEffect.receiveAsFlow()

    fun processIntent(intent: DetailIntent) {
        when (intent) {
            is DetailIntent.LoadTopic -> loadTopic(intent.id)
            is DetailIntent.GoBack -> emitSideEffect(DetailSideEffect.NavigateBack)
        }
    }

    private fun loadTopic(id: Int) {
        reduce { copy(isLoading = true) }
        viewModelScope.launch {
            try {
                val topic = getTopicByIdUseCase(id)
                reduce { copy(isLoading = false, topic = topic) }
            } catch (e: Exception) {
                reduce { copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    private fun reduce(reducer: DetailUiState.() -> DetailUiState) {
        _uiState.update { it.reducer() }
    }

    private fun emitSideEffect(effect: DetailSideEffect) {
        viewModelScope.launch { _sideEffect.send(effect) }
    }
}

