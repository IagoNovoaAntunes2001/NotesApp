package com.notes.home.domain.usecases

import com.notes.home.domain.entities.Topic
import com.notes.home.domain.repositories.TopicRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetTopicByIdUseCase(
    private val topicRepository: TopicRepository,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default
) {
    suspend operator fun invoke(id: Int): Topic? = withContext(defaultDispatcher) {
        topicRepository.getTopicById(id)
    }
}

