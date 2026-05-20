package com.notes.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.notes.core.data.sync.SyncConstants
import com.notes.core.data.usecase.SyncTopicsUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * SyncDownWorker — Fase 2 da cadeia de sincronização.
 *
 * Responsabilidade: PULL — busca dados novos do servidor, aplica LWW no Room.
 * Só executa se SyncUpWorker terminou com Result.success().
 *
 * Cadeia: SyncUpWorker → SyncDownWorker → CleanupWorker
 *
 * inputData: recebe KEY_UP_DONE do SyncUpWorker
 * outputData: passa KEY_DOWN_DONE para o CleanupWorker
 */
@HiltWorker
class SyncDownWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val syncTopicsUseCase: SyncTopicsUseCase
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        setProgress(workDataOf(SyncConstants.KEY_PROGRESS to 33))

        // Lê dado passado pelo SyncUpWorker via outputData
        @Suppress("UNUSED_VARIABLE")
        val upDone = inputData.getBoolean(SyncUpWorker.KEY_UP_DONE, false)

        return syncTopicsUseCase()
            .fold(
                onSuccess = {
                    setProgress(workDataOf(SyncConstants.KEY_PROGRESS to 66))
                    Result.success(workDataOf(KEY_DOWN_DONE to true))
                },
                onFailure = { Result.retry() }
            )
    }

    companion object {
        const val WORK_NAME = "sync_down"
        const val KEY_DOWN_DONE = "sync_down_done"
    }
}

