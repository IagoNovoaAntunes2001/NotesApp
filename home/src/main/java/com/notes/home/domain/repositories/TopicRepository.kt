package com.notes.home.domain.repositories

import com.notes.home.domain.entities.Topic

interface TopicRepository {
    fun getTopics(): List<Topic>
}
