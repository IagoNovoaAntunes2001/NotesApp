package com.notes.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.notes.core.data.repository.TopicRepository
import com.notes.core.data.sync.SyncConstants
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * CleanupWorker — Fase 3 (final) da cadeia de sincronização.
 *
 * Responsabilidade: limpar do Room dados obsoletos após sync completo.
 *   - Remove tópicos com syncStatus = ERROR há mais de 7 dias
 *
 * Só executa se SyncDownWorker terminou com Result.success().
 *
 * Cadeia: SyncUpWorker → SyncDownWorker → CleanupWorker
 *
 * inputData: recebe KEY_DOWN_DONE do SyncDownWorker
 */
@HiltWorker
class CleanupWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val topicRepository: TopicRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        setProgress(workDataOf(SyncConstants.KEY_PROGRESS to 66))

        // Lê dado passado pelo SyncDownWorker
        @Suppress("UNUSED_VARIABLE")
        val downDone = inputData.getBoolean(SyncDownWorker.KEY_DOWN_DONE, false)

        return topicRepository.cleanup()
            .fold(
                onSuccess = {
                    // Progresso 100% — cadeia completa!
                    setProgress(workDataOf(SyncConstants.KEY_PROGRESS to 100))
                    Result.success()
                },
                onFailure = {
                    // Cleanup pode falhar sem ser crítico (apenas limpeza)
                    // Usamos Result.success() para não bloquear a cadeia na próxima execução
                    Result.success()
                }
            )
    }

    companion object {
        const val WORK_NAME = "sync_cleanup"
    }
}

