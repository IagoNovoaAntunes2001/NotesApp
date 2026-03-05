package com.notes.home.data.repositories

import com.notes.home.data.mappers.toEntity
import com.notes.home.data.remote.dto.TopicDto
import com.notes.home.domain.entities.Topic
import com.notes.home.domain.repositories.TopicRepository

class TopicRepositoryImpl : TopicRepository {
    override fun getTopics(): List<Topic> = listOf(
        TopicDto(
            id = 1,
            title = "Kotlin Coroutines",
            description = "Learn about asynchronous programming with Kotlin Coroutines."
        ).toEntity(),
        TopicDto(
            id = 2,
            title = "Jetpack Compose",
            description = "Discover how to build modern Android UIs with Jetpack Compose."
        ).toEntity(),
        TopicDto(
            id = 3,
            title = "Dependency Injection",
            description = "Understand the benefits of Dependency Injection and how to implement it in Android."
        ).toEntity()
    )
}
