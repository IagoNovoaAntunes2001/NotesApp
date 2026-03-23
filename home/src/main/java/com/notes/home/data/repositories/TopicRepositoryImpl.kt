package com.notes.home.data.repositories

import com.notes.home.data.local.dao.TopicDao
import com.notes.home.data.mappers.toDomain
import com.notes.home.data.mappers.toEntity
import com.notes.home.domain.entities.Topic
import com.notes.home.domain.repositories.TopicRepository

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
