package com.notes.core.data.sync

/**
 * Constantes compartilhadas entre o SyncWorker (:app) e os observadores (ex: :home).
 * Ficam em :core:data para que qualquer módulo possa importar sem criar dependências circulares.
 */
object SyncConstants {
    const val WORK_NAME = "sync_topics_periodic"
    const val KEY_PROGRESS = "sync_progress" // Int: 0..100

    // Nome único da cadeia: SyncUpWorker → SyncDownWorker → CleanupWorker
    const val SYNC_CHAIN_NAME = "sync_chain"
}
