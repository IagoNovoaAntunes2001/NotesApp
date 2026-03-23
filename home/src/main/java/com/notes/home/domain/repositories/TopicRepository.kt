package com.notes.home.domain.repositories

import com.notes.home.domain.entities.Topic

interface TopicRepository {
    suspend fun getTopics(): List<Topic>
    suspend fun getTopicById(id: Int): Topic?
    suspend fun insertTopic(topic: Topic)
    suspend fun deleteTopic(topic: Topic)
}
