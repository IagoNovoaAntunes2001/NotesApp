package com.notes.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.notes.core.data.sync.SyncConstants
import com.notes.core.data.usecase.SyncPendingTopicsUseCase
import com.notes.core.data.usecase.SyncTopicsUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * SyncWorker — responsável pela sincronização completa em background.
 *
 * Fase 1 — PUSH: envia ao servidor todos os tópicos com syncStatus = PENDING
 *   (edições feitas offline que ainda não chegaram ao servidor)
 *
 * Fase 2 — PULL: busca dados novos do servidor e aplica LWW no Room
 *   (novos dados ou dados mais recentes do servidor vencem o local)
 *
 * Retry com Exponential Backoff (configurado no NotesApplication):
 *   Falha → retorna Result.retry()
 *   WorkManager agenda nova tentativa: 10s → 20s → 40s → 80s → ... (até o limite do sistema)
 *
 * Constraints (configurados no NotesApplication):
 *   - requiresNetwork: só roda com internet disponível
 *   - requiresBatteryNotLow: não roda se bateria está fraca
 */
@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val syncPendingTopicsUseCase: SyncPendingTopicsUseCase,
    private val syncTopicsUseCase: SyncTopicsUseCase
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        // Progresso: 0% — início
        setProgress(workDataOf(KEY_PROGRESS to 0))

        // Fase 1: PUSH — envia pendentes ao servidor
        syncPendingTopicsUseCase()
            .onFailure { return Result.retry() }

        // Progresso: 50% — push concluído
        setProgress(workDataOf(SyncConstants.KEY_PROGRESS to 50))

        // Fase 2: PULL — busca dados novos do servidor (aplica LWW no Room)
        syncTopicsUseCase()
            .onFailure { return Result.retry() }

        // Progresso: 100% — tudo sincronizado
        setProgress(workDataOf(SyncConstants.KEY_PROGRESS to 100))

        return Result.success()
    }

    companion object {
        // Aliases para retrocompatibilidade — a fonte da verdade é SyncConstants
        const val WORK_NAME = SyncConstants.WORK_NAME
        const val KEY_PROGRESS = SyncConstants.KEY_PROGRESS
    }
}
