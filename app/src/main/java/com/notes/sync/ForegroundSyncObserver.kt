package com.notes.sync

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager

/**
 * ForegroundSyncObserver — dispara sync urgente toda vez que o app volta ao foreground.
 *
 * ## Como funciona o Lifecycle do Processo:
 *
 * O Android tem dois tipos de Lifecycle:
 *
 * 1. Activity Lifecycle (onStart/onStop por Activity):
 *    - Dispara toda vez que uma Activity específica aparece/desaparece
 *    - Problema: ao navegar entre telas, onStop/onStart disparam desnecessariamente
 *
 * 2. Process Lifecycle (ProcessLifecycleOwner):
 *    - Representa TODO o app (todas as Activities juntas)
 *    - onStart → app voltou ao foreground (de background total)
 *    - onStop  → app foi para background (usuário saiu do app)
 *    - NÃO dispara ao navegar entre telas — apenas ao entrar/sair do app
 *
 * ## Por que isso é útil?
 *   Quando o usuário sai do app por X minutos e volta:
 *   - Pode haver dados novos no servidor (outro device editou)
 *   - Pode haver dados PENDING que falharam antes
 *   → sync imediato ao voltar = dados sempre frescos
 *
 * ## Fluxo:
 *   App em background → usuário abre → ProcessLifecycleOwner.onStart()
 *   → ForegroundSyncObserver.onStart() → enfileira UrgentSyncWorker
 *   → Expedited: roda em segundos → UI atualiza via Room Flow
 */
class ForegroundSyncObserver(
    private val workManager: WorkManager
) : DefaultLifecycleObserver {

    /**
     * Chamado quando o app VOLTA ao foreground.
     *
     * DefaultLifecycleObserver nos permite sobrescrever apenas os métodos que queremos,
     * sem precisar implementar todos (diferente de LifecycleObserver com @OnLifecycleEvent).
     */
    override fun onStart(owner: LifecycleOwner) {
        enqueueUrgentSync()
    }

    private fun enqueueUrgentSync() {
        val urgentRequest = OneTimeWorkRequestBuilder<UrgentSyncWorker>()
            .setExpedited(
                // Se a cota de expedited acabou → degrada para worker normal (não cancela)
                OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST
            )
            // Sem constraints de rede aqui propositalmente:
            // O UrgentSyncWorker vai falhar com Result.retry() se não tiver rede,
            // e o backoff exponencial vai tentar novamente quando a rede voltar.
            .build()

        workManager.enqueueUniqueWork(
            UrgentSyncWorker.WORK_NAME,
            // REPLACE: se já existe um sync urgente em andamento, cancela e começa do zero
            // (o usuário acabou de abrir o app — queremos dados frescos agora)
            ExistingWorkPolicy.REPLACE,
            urgentRequest
        )
    }
}

