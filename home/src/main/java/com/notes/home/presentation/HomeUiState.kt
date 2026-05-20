package com.notes.home.presentation

import com.notes.core.model.Topic

/**
 * UiState modelado para os 3 cenários offline-first:
 *
 * Cenário 1 — Online + cache:
 *   isRefreshing=true, topics.isNotEmpty() → mostra lista + indicador de sync
 *
 * Cenário 2 — Offline + cache:
 *   isOffline=true, topics.isNotEmpty() → mostra lista + banner "Sem internet"
 *
 * Cenário 3 — Offline + sem cache:
 *   isOffline=true, topics.isEmpty(), syncFailed=true → tela de erro
 */
internal data class HomeUiState(
    val isLoading: Boolean = false,
    val topics: List<Topic> = emptyList(),
    val searchQuery: String = "",
    val errorMessage: String? = null,

    // Estado atual do SyncWorker observado via WorkInfo
    // Substitui os campos soltos isRefreshing + syncProgress
    val syncState: SyncState = SyncState.Idle,

    // Sem conexão detectada via falha no sync
    val isOffline: Boolean = false,

    // Sync falhou (API error, timeout, etc.)
    val syncFailed: Boolean = false
)
