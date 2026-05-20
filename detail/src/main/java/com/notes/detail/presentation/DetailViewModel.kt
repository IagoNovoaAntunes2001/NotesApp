package com.notes.detail.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notes.core.data.usecase.GetTopicByIdUseCase
import com.notes.core.data.usecase.UpdateTopicUseCase
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
    private val getTopicByIdUseCase: GetTopicByIdUseCase,
    private val updateTopicUseCase: UpdateTopicUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    private val _sideEffect = Channel<DetailSideEffect>()
    val sideEffect = _sideEffect.receiveAsFlow()

    fun processIntent(intent: DetailIntent) {
        when (intent) {
            is DetailIntent.LoadTopic       -> loadTopic(intent.id)
            is DetailIntent.GoBack          -> emitSideEffect(DetailSideEffect.NavigateBack)
            is DetailIntent.StartEditing    -> startEditing()
            is DetailIntent.CancelEditing   -> cancelEditing()
            is DetailIntent.TitleChanged    -> reduce { copy(editedTitle = intent.value) }
            is DetailIntent.DescriptionChanged -> reduce { copy(editedDescription = intent.value) }
            is DetailIntent.SaveTopic       -> saveTopic()
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

    /** Entra no modo edição, populando os campos com os dados atuais. */
    private fun startEditing() {
        val topic = _uiState.value.topic ?: return
        reduce {
            copy(
                isEditing = true,
                editedTitle = topic.title,
                editedDescription = topic.description,
            )
        }
    }

    /** Cancela sem salvar — restaura os campos e volta ao modo visualização. */
    private fun cancelEditing() {
        reduce { copy(isEditing = false, editedTitle = "", editedDescription = "") }
    }

    /**
     * OPTIMISTIC UPDATE:
     *
     * Passo 1 — UI atualiza IMEDIATAMENTE (antes de qualquer I/O).
     *   O usuário vê o dado novo na hora, sem spinner de bloqueio.
     *   Isso é a essência do "optimistic": assumimos que vai funcionar.
     *
     * Passo 2 — Persiste no Room + envia para API em background (isSaving = true).
     *   Room é a fonte de verdade → salva primeiro.
     *   API vem depois → se falhar, snackbar avisa (rollback = Terça-feira).
     *
     * Por que não esperar a confirmação antes de atualizar a UI?
     *   Latência de rede pode ser 200ms-2s. Segurar a UI nesse tempo é ruim.
     *   Apps como Gmail, Google Docs, Notion todos fazem isso.
     */
    private fun saveTopic() {
        val currentTopic = _uiState.value.topic ?: return
        val newTitle = _uiState.value.editedTitle.trim()
        val newDescription = _uiState.value.editedDescription.trim()

        if (newTitle.isBlank()) {
            emitSideEffect(DetailSideEffect.ShowSnackbar("Título não pode ser vazio"))
            return
        }

        val updatedTopic = currentTopic.copy(title = newTitle, description = newDescription)

        // ── PASSO 1: Optimistic ──────────────────────────────────────────────
        // UI reflete o dado novo AGORA, sem esperar Room ou API.
        reduce {
            copy(
                isEditing = false,      // sai do modo edição instantaneamente
                isSaving = true,        // indicador sutil de sync em background
                topic = updatedTopic,   // ← dado novo já visível na UI!
                editedTitle = "",
                editedDescription = "",
            )
        }

        // ── PASSO 2: Background I/O ──────────────────────────────────────────
        // Room + API rodam em background — UI já estava atualizada no passo 1.
        viewModelScope.launch {
            updateTopicUseCase(updatedTopic).fold(
                onSuccess = {
                    reduce { copy(isSaving = false) }
                    emitSideEffect(DetailSideEffect.ShowSnackbar("Salvo com sucesso ✓"))
                },
                onFailure = {
                    // ROLLBACK: o Repository já reverteu o Room para o snapshot anterior.
                    // Aqui revertemos o UIState para refletir isso — o Flow do Room
                    // também vai emitir o dado antigo automaticamente, mas revertemos
                    // o topic no state explicitamente para garantir consistência imediata.
                    reduce {
                        copy(
                            isSaving = false,
                            topic = currentTopic, // ← reverte para o dado original
                        )
                    }
                    emitSideEffect(DetailSideEffect.ShowSnackbar("Erro ao salvar. Alteração revertida."))
                }
            )
        }
    }

    private fun reduce(reducer: DetailUiState.() -> DetailUiState) {
        _uiState.update { it.reducer() }
    }

    private fun emitSideEffect(effect: DetailSideEffect) {
        viewModelScope.launch { _sideEffect.send(effect) }
    }
}
