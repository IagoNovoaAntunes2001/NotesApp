package com.notes.home.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notes.core.data.usecase.AddTopicUseCase
import com.notes.core.data.usecase.DeleteTopicUseCase
import com.notes.core.data.usecase.GetTopicsStreamUseCase
import com.notes.core.data.usecase.SyncTopicsUseCase
import com.notes.core.model.AppResult
import com.notes.core.model.Topic
import com.notes.home.presentation.resources.HomeResources
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel que implementa os 3 cenários offline-first:
 *
 * Cenário 1 — Online + cache:
 *   → Room emite dados + sync em background → isRefreshing enquanto sincroniza
 *
 * Cenário 2 — Offline + cache:
 *   → Room emite dados (cache) + sync falha → isOffline=true, banner de aviso
 *
 * Cenário 3 — Offline + sem cache:
 *   → Room emite lista vazia + sync falha → syncFailed=true, tela de erro
 */
@HiltViewModel
internal class HomeViewModel @Inject constructor(
    private val getTopicsStreamUseCase: GetTopicsStreamUseCase,
    private val syncTopicsUseCase: SyncTopicsUseCase,
    private val addTopicUseCase: AddTopicUseCase,
    private val deleteTopicUseCase: DeleteTopicUseCase,
    private val savedStateHandle: SavedStateHandle,
    private val resources: HomeResources
) : ViewModel() {

    companion object {
        private const val KEY_SEARCH_QUERY = "search_query"
    }

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    var searchQuery: String
        get() = savedStateHandle.get<String>(KEY_SEARCH_QUERY) ?: ""
        set(value) {
            savedStateHandle[KEY_SEARCH_QUERY] = value
            reduce { copy(searchQuery = value) }
        }

    private val _sideEffect = Channel<HomeSideEffect>()
    val sideEffect = _sideEffect.receiveAsFlow()

    init {
        // Inicia a observação reativa do Room
        observeTopicsStream()
        // Dispara sync imediato ao abrir o app
        syncTopics()
    }

    fun processIntent(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.LoadTopics -> syncTopics()
            is HomeIntent.Refresh -> syncTopics()
            is HomeIntent.AddTopic -> addTopic(intent.title, intent.description)
            is HomeIntent.DeleteTopic -> deleteTopic(intent.topic)
            is HomeIntent.Search -> { searchQuery = intent.query }
            is HomeIntent.NavigateToDetail -> {
                emitSideEffect(HomeSideEffect.NavigateToDetail(intent.topic.id))
            }
        }
    }

    /**
     * Observa o Room de forma reativa.
     * Qualquer INSERT/UPDATE/DELETE no Room emite aqui automaticamente.
     *
     * Flow emite AppResult:
     * - Loading  → Room ainda não respondeu
     * - Success  → dados atualizados (lista do cache ou recém-sincronizados)
     * - Error    → erro no Room (raro)
     */
    private fun observeTopicsStream() {
        viewModelScope.launch {
            getTopicsStreamUseCase().collect { result ->
                when (result) {
                    is AppResult.Loading -> {
                        reduce { copy(isLoading = true) }
                    }
                    is AppResult.Success -> {
                        reduce {
                            copy(
                                isLoading = false,
                                topics = result.data,
                                errorMessage = null
                            )
                        }
                    }
                    is AppResult.Error -> {
                        reduce {
                            copy(
                                isLoading = false,
                                errorMessage = result.exception.message
                            )
                        }
                    }
                }
            }
        }
    }

    /**
     * Sincroniza com a API (background).
     *
     * Cenário 1 (Online+cache): sync sucesso → Room atualiza → Flow emite → UI atualiza
     * Cenário 2 (Offline+cache): sync falha + topics.isNotEmpty → isOffline=true, banner
     * Cenário 3 (Offline+sem cache): sync falha + topics.isEmpty → syncFailed=true, tela erro
     */
    private fun syncTopics() {
        viewModelScope.launch {
            // Mostra indicador de sync em background (não bloqueia a UI)
            reduce { copy(isRefreshing = true, isOffline = false, syncFailed = false) }

            val syncResult = syncTopicsUseCase()

            syncResult.fold(
                onSuccess = {
                    // Cenário 1: online, dados frescos chegaram via Room Flow automaticamente
                    reduce { copy(isRefreshing = false, isOffline = false, syncFailed = false) }
                },
                onFailure = { exception ->
                    val hasCachedData = _uiState.value.topics.isNotEmpty()
                    reduce {
                        copy(
                            isRefreshing = false,
                            isOffline = true,
                            syncFailed = !hasCachedData,
                            // Cenário 2: tem cache → não mostra errorMessage (UI mostra banner)
                            // Cenário 3: sem cache → mostra mensagem de erro
                            errorMessage = if (hasCachedData) null else exception.message
                        )
                    }

                    if (hasCachedData) {
                        // Cenário 2: avisa mas não bloqueia
                        emitSideEffect(HomeSideEffect.ShowSnackbar(resources.offlineWithCache))
                    }
                }
            )
        }
    }

    private fun addTopic(title: String, description: String) {
        viewModelScope.launch {
            try {
                addTopicUseCase(title, description)
                emitSideEffect(HomeSideEffect.ShowSnackbar(resources.topicAddedSuccess))
            } catch (e: Exception) {
                emitSideEffect(HomeSideEffect.ShowError(e.message ?: resources.errorAddTopic))
            }
        }
    }

    private fun deleteTopic(topic: Topic) {
        viewModelScope.launch {
            try {
                deleteTopicUseCase(topic)
                emitSideEffect(HomeSideEffect.ShowSnackbar(resources.topicDeletedSuccess))
            } catch (e: Exception) {
                emitSideEffect(HomeSideEffect.ShowError(e.message ?: resources.errorDeleteTopic))
            }
        }
    }

    private fun reduce(reducer: HomeUiState.() -> HomeUiState) {
        _uiState.update { it.reducer() }
    }

    private fun emitSideEffect(effect: HomeSideEffect) {
        viewModelScope.launch { _sideEffect.send(effect) }
    }
}
