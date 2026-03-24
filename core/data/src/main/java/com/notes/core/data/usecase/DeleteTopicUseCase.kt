package com.notes.core.data.usecase

import com.notes.core.data.repository.TopicRepository
import com.notes.core.model.Topic
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DeleteTopicUseCase(
    private val topicRepository: TopicRepository,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend operator fun invoke(topic: Topic) {
        withContext(ioDispatcher) {
            topicRepository.deleteTopic(topic)
        }
    }
}
