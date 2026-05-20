package com.notes.sync

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.notes.core.data.sync.SyncConstants
import com.notes.core.data.usecase.SyncPendingTopicsUseCase
import com.notes.core.data.usecase.SyncTopicsUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * UrgentSyncWorker — Sincronização de ALTA PRIORIDADE via Expedited Work.
 *
 * ## O que é Expedited Work?
 * É um tipo especial de WorkRequest que recebe prioridade máxima do sistema.
 * Enquanto um worker normal pode esperar minutos/horas (doze mode, job scheduler),
 * o Expedited roda o mais rápido possível, quase imediatamente.
 *
 * ## Quando usar?
 * - Usuário acabou de criar/editar um dado importante → queremos garantir que chegou ao servidor
 * - Usuário está saindo do app (foreground → background) → último sync urgente
 * - Reconexão de rede → sync imediato de tudo que ficou pendente
 *
 * ## Regras do Expedited Work:
 * 1. DEVE implementar getForegroundInfo() — o sistema pode precisar rodar como foreground service
 * 2. Tem limite de execução (máx ~10min no Android 12+)
 * 3. NÃO suporta chaining (é sempre OneTimeWork individual)
 * 4. Não respeita constraints de bateria (propositalmente — é urgente)
 *
 * ## Diferença do worker normal:
 *   Worker normal:    .enqueue() → sistema agenda → pode esperar horas
 *   Expedited:        .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
 *                     → sistema prioriza → roda em segundos
 *
 * ## OutOfQuotaPolicy:
 *   Se o app já usou toda a cota de expedited work:
 *   - RUN_AS_NON_EXPEDITED_WORK_REQUEST → degrada para worker normal (não falha)
 *   - DROP_WORK_REQUEST → cancela (use quando o trabalho ficou obsoleto)
 */
@HiltWorker
class UrgentSyncWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val syncPendingTopicsUseCase: SyncPendingTopicsUseCase,
    private val syncTopicsUseCase: SyncTopicsUseCase
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        setProgress(workDataOf(SyncConstants.KEY_PROGRESS to 0))

        // Fase 1: PUSH — envia pendentes
        syncPendingTopicsUseCase()
            .onFailure { return Result.retry() }

        setProgress(workDataOf(SyncConstants.KEY_PROGRESS to 50))

        // Fase 2: PULL — busca dados novos
        syncTopicsUseCase()
            .onFailure { return Result.retry() }

        setProgress(workDataOf(SyncConstants.KEY_PROGRESS to 100))

        return Result.success()
    }

    /**
     * Obrigatório para Expedited Work.
     *
     * Por quê? O WorkManager pode precisar rodar este worker como um
     * ForegroundService (especialmente em Android < 12 ou em background restrito).
     * Para isso, ele precisa de uma Notification para mostrar ao usuário.
     *
     * Sem isso: IllegalStateException ao tentar rodar como expedited.
     */
    override suspend fun getForegroundInfo(): ForegroundInfo {
        createNotificationChannelIfNeeded()
        val notification = buildNotification()

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            ForegroundInfo(NOTIFICATION_ID, notification)
        }
    }

    private fun createNotificationChannelIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Sincronização urgente",
                NotificationManager.IMPORTANCE_LOW  // LOW = sem som, mas visível
            )
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification =
        NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Sincronizando...")
            .setContentText("Salvando suas anotações na nuvem")
            .setSmallIcon(android.R.drawable.ic_popup_sync)
            .setOngoing(true)  // não pode ser dispensada pelo usuário enquanto roda
            .build()

    companion object {
        const val WORK_NAME = "urgent_sync"
        private const val NOTIFICATION_ID = 42
        private const val CHANNEL_ID = "urgent_sync_channel"
    }
}

