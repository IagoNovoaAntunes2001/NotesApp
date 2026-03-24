package com.notes.core.data.repository

import com.notes.core.model.Topic

interface TopicRepository {
    suspend fun getTopics(): List<Topic>
    suspend fun getTopicById(id: Int): Topic?
    suspend fun insertTopic(topic: Topic)
    suspend fun deleteTopic(topic: Topic)
}
