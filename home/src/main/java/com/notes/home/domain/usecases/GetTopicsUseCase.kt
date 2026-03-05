package com.notes.home.domain.usecases

import com.notes.home.domain.repositories.TopicRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetTopicsUseCase(
    private val topicRepository: TopicRepository,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default
) {
    suspend operator fun invoke() = withContext(defaultDispatcher) {
        topicRepository.getTopics()
    }
}
