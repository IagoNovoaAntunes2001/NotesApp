package com.notes

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.work.BackoffPolicy
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.notes.sync.CleanupWorker
import com.notes.sync.ForegroundSyncObserver
import com.notes.sync.SyncDownWorker
import com.notes.sync.SyncUpWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject

// @HiltAndroidApp dispara a geração de código do Hilt e inicializa
// o grafo de dependências global (SingletonComponent) automaticamente.
@HiltAndroidApp
class NotesApplication : Application(), Configuration.Provider {

    // Hilt injeta a factory que sabe criar Workers com dependências (@HiltWorker)
    @Inject lateinit var workerFactory: HiltWorkerFactory

    // Fornece configuração customizada ao WorkManager (com HiltWorkerFactory)
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        scheduleSyncChain()
        schedulePeriodicSync()
        registerForegroundObserver()
    }

    /**
     * Cadeia OneTime: SyncUpWorker → SyncDownWorker → CleanupWorker
     * Roda ao iniciar o app. Se já existe uma cadeia pendente (KEEP), não recria.
     */
    private fun scheduleSyncChain() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()

        val syncUp = OneTimeWorkRequestBuilder<SyncUpWorker>()
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 10, TimeUnit.SECONDS)
            .build()

        val syncDown = OneTimeWorkRequestBuilder<SyncDownWorker>()
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 10, TimeUnit.SECONDS)
            .build()

        val cleanup = OneTimeWorkRequestBuilder<CleanupWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this)
            .beginUniqueWork(SYNC_CHAIN_NAME, ExistingWorkPolicy.KEEP, syncUp)
            .then(syncDown)
            .then(cleanup)
            .enqueue()
    }

    /**
     * Sync Periódico: roda a cada 15 minutos (mínimo permitido pelo Android).
     *
     * ## Por que 15 min é o mínimo?
     * O Android impõe esse limite para economizar bateria.
     * Mesmo que você peça 5min, o WorkManager vai usar 15min.
     *
     * ## PeriodicWork vs OneTimeWork:
     *   OneTimeWork:  roda uma vez → pode encadear (chaining) ✅
     *   PeriodicWork: roda repetidamente → NÃO suporta chaining ❌
     *
     * Por isso o PeriodicWork aqui usa o SyncUpWorker diretamente (worker simples),
     * sem a cadeia completa. A cadeia completa é acionada pelo ForegroundObserver
     * e na abertura do app (scheduleSyncChain).
     *
     * ## ExistingPeriodicWorkPolicy:
     *   KEEP:   já existe? mantém o existente (não reinicia o timer)
     *   UPDATE: já existe? atualiza constraints/interval sem perder a próxima execução
     *   CANCEL_AND_REENQUEUE: cancela e recria do zero (reinicia o timer)
     */
    private fun schedulePeriodicSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val periodicRequest = PeriodicWorkRequestBuilder<SyncUpWorker>(
            repeatInterval = 15,
            repeatIntervalTimeUnit = TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 10, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            PERIODIC_SYNC_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            periodicRequest
        )
    }

    /**
     * Registra o ForegroundSyncObserver no ProcessLifecycleOwner.
     *
     * ProcessLifecycleOwner observa o lifecycle do PROCESSO todo,
     * não de uma Activity específica. Isso significa:
     *   - onStart: app voltou ao foreground (de background total)
     *   - onStop: app foi para background
     *   - Não dispara ao navegar entre telas
     *
     * Perfeito para disparar sync urgente quando o usuário abre o app.
     */
    private fun registerForegroundObserver() {
        val workManager = WorkManager.getInstance(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(
            ForegroundSyncObserver(workManager)
        )
    }

    companion object {
        const val SYNC_CHAIN_NAME = "sync_chain" // nome único da cadeia
        const val PERIODIC_SYNC_NAME = "periodic_sync"
    }
}
