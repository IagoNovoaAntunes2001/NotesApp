package com.notes.detail.presentation

internal sealed interface DetailIntent {
    data class LoadTopic(val id: Int) : DetailIntent
    data object GoBack : DetailIntent

    // Inicia modo de edição (preenche campos com dados atuais)
    data object StartEditing : DetailIntent

    // Cancela edição sem salvar
    data object CancelEditing : DetailIntent

    // Usuário digitou algo nos campos
    data class TitleChanged(val value: String) : DetailIntent
    data class DescriptionChanged(val value: String) : DetailIntent

    // Confirma a edição → dispara optimistic update
    data object SaveTopic : DetailIntent
}
