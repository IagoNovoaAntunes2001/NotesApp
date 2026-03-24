package com.notes.core.data.usecase

import com.notes.core.data.repository.TopicRepository
import com.notes.core.model.Topic
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetTopicsUseCase(
    private val topicRepository: TopicRepository,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default
) {
    suspend operator fun invoke(): List<Topic> = withContext(defaultDispatcher) {
        topicRepository.getTopics()
    }
}
