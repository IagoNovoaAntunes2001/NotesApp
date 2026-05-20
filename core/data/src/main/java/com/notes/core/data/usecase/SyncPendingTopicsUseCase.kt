package com.notes.core.data.usecase

import com.notes.core.data.repository.TopicRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Responsabilidade única: enviar ao servidor todos os tópicos com syncStatus = PENDING.
 *
 * Chamado pelo SyncWorker como fase de "push" antes do "pull" (sync geral).
 * Separado do SyncTopicsUseCase (que faz pull) para facilitar testes e reuso.
 */
class SyncPendingTopicsUseCase(
    private val topicRepository: TopicRepository,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend operator fun invoke(): Result<Unit> = withContext(ioDispatcher) {
        topicRepository.syncPending()
    }
}
