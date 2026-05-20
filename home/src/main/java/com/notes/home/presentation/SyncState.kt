package com.notes.home.presentation

/**
 * Representa todos os estados possíveis do SyncWorker observados via WorkInfo.
 *
 * WorkInfo.State mapeado para UX:
 *
 *   BLOCKED   → worker está na cadeia mas aguardando o anterior terminar → Enqueued
 *   ENQUEUED  → agendado, aguardando constraints (rede, bateria)        → Enqueued
 *   RUNNING   → executando agora, reporta progresso (0..100)            → Running
 *   SUCCEEDED → terminou com sucesso                                    → Succeeded
 *   FAILED    → terminou com falha (esgotou retries)                    → Failed
 *   CANCELLED → cancelado manualmente                                   → Idle
 *   null      → nenhum worker registrado / estado desconhecido          → Idle
 */
sealed interface SyncState {

    /** Nenhum worker ativo — estado padrão (antes do primeiro sync ou após cancelamento) */
    data object Idle : SyncState

    /** Worker agendado, aguardando constraints (rede, bateria) ou worker anterior da cadeia */
    data object Enqueued : SyncState

    /**
     * Worker executando agora.
     * @param progress Percentual de conclusão (0..100).
     *   0%  → SyncUpWorker iniciou (PUSH)
     *   33% → SyncUpWorker concluiu
     *   66% → SyncDownWorker concluiu (PULL)
     *   100% → CleanupWorker concluiu
     */
    data class Running(val progress: Int = 0) : SyncState

    /** Cadeia completa concluída com sucesso */
    data object Succeeded : SyncState

    /** Worker falhou após esgotar todas as tentativas de retry */
    data object Failed : SyncState
}

