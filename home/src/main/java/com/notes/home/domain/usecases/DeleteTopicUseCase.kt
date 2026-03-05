package com.notes.home.domain.usecases

import com.notes.home.domain.entities.Topic
import com.notes.home.domain.repositories.TopicRepository
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

