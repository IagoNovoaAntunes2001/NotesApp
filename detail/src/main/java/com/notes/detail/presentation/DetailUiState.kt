package com.notes.detail.presentation

import com.notes.core.model.Topic

/**
 * Estado da tela de detalhe com suporte a edição + optimistic update.
 *
 * Modos da tela:
 *   isEditing = false → modo visualização (título + descrição como Text)
 *   isEditing = true  → modo edição (campos TextField editáveis)
 *
 * Optimistic update:
 *   Quando o usuário salva, [topic] é atualizado imediatamente no estado
 *   ANTES de confirmar Room ou API. A UI já reflete o dado novo.
 *   [isSaving] = true enquanto Room + API processam em background.
 */
internal data class DetailUiState(
    val isLoading: Boolean = false,
    val topic: Topic? = null,
    val errorMessage: String? = null,

    // Estado de edição
    val isEditing: Boolean = false,
    val editedTitle: String = "",
    val editedDescription: String = "",

    // true enquanto Room + API processam em background após salvar
    val isSaving: Boolean = false
)
