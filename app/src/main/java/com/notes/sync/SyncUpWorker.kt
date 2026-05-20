package com.notes.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.notes.core.data.sync.SyncConstants
import com.notes.core.data.usecase.SyncPendingTopicsUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * SyncUpWorker — Fase 1 da cadeia de sincronização.
 *
 * Responsabilidade: PUSH
 *   Envia ao servidor todos os tópicos com syncStatus = PENDING
 *   (edições feitas offline que ainda não chegaram ao servidor).
 *
 * Cadeia:
 *   SyncUpWorker → SyncDownWorker → CleanupWorker
 *
 * Passagem de dados para o próximo worker:
 *   Saída: KEY_SYNCED_COUNT (Int) — quantos tópicos foram enviados
 *   O SyncDownWorker recebe isso via inputData para fins de log/debug.
 *
 * Se falhar → Result.retry() (backoff exponencial configurado no Application)
 * Se sucesso → SyncDownWorker é disparado automaticamente pelo WorkManager
 */
@HiltWorker
class SyncUpWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val syncPendingTopicsUseCase: SyncPendingTopicsUseCase
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        setProgress(workDataOf(SyncConstants.KEY_PROGRESS to 0))

        return syncPendingTopicsUseCase()
            .fold(
                onSuccess = {
                    setProgress(workDataOf(SyncConstants.KEY_PROGRESS to 33))

                    // Passa dado para o próximo worker da cadeia via outputData
                    // O SyncDownWorker pode ler com inputData.getBoolean(KEY_UP_DONE, false)
                    Result.success(
                        workDataOf(KEY_UP_DONE to true)
                    )
                },
                onFailure = {
                    // Falha → WorkManager aplica backoff exponencial e tenta de novo
                    // SyncDownWorker e CleanupWorker NÃO serão executados
                    Result.retry()
                }
            )
    }

    companion object {
        const val WORK_NAME = "sync_up"
        const val KEY_UP_DONE = "sync_up_done" // Boolean passado para SyncDownWorker
    }
}

