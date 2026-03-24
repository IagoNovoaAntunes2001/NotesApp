package com.notes.core.database.repository

import com.notes.core.data.repository.TopicRepository
import com.notes.core.database.dao.TopicDao
import com.notes.core.database.mapper.toDomain
import com.notes.core.database.mapper.toEntity
import com.notes.core.model.Topic

class TopicRepositoryImpl(
    private val topicDao: TopicDao
) : TopicRepository {

    override suspend fun getTopics(): List<Topic> =
        topicDao.getAll().map { it.toDomain() }

    override suspend fun getTopicById(id: Int): Topic? =
        topicDao.getById(id)?.toDomain()

    override suspend fun insertTopic(topic: Topic) =
        topicDao.insert(topic.toEntity())

    override suspend fun deleteTopic(topic: Topic) =
        topicDao.delete(topic.toEntity())
}
