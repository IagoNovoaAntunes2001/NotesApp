package com.notes.core.data.usecase

import com.notes.core.data.repository.TopicRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SyncTopicsUseCase(
    private val topicRepository: TopicRepository,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend operator fun invoke(): Result<Unit> = withContext(ioDispatcher) {
        topicRepository.sync()
    }
}

