package com.notes.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.notes.core.data.usecase.SyncTopicsUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val syncTopicsUseCase: SyncTopicsUseCase
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return syncTopicsUseCase()
            .fold(
                onSuccess = { Result.success() },
                onFailure = { Result.retry() }  // backoff exponencial entra aqui
            )
    }

    companion object {
        const val WORK_NAME = "sync_topics_periodic"
    }
}

