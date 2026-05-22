package com.notes.core.network.datasource

import com.notes.core.network.dto.TopicDto
import com.notes.core.network.pagination.PagedResult

/**
 * Fake implementation do RemoteDataSource.
 *
 * Simula uma API real com dados em memória.
 * Usada enquanto não há servidor real e nos testes instrumentados.
 *
 * Para usar em vez da real, basta trocar o @Binds no NetworkModule.
 */
class FakeTopicRemoteDataSource : TopicRemoteDataSource {

    // "Banco de dados" em memória — simula a API
    private val fakeDb = mutableListOf(
        TopicDto(id = 1, title = "Kotlin Coroutines",   description = "Async programming in Kotlin"),
        TopicDto(id = 2, title = "Jetpack Compose",     description = "Modern UI toolkit for Android"),
        TopicDto(id = 3, title = "Room Database",       description = "SQLite abstraction layer"),
        TopicDto(id = 4, title = "Hilt",                description = "Dependency injection for Android"),
        TopicDto(id = 5, title = "WorkManager",         description = "Background tasks that are guaranteed"),
    )

    override suspend fun fetchTopics(): List<TopicDto> {
        kotlinx.coroutines.delay(50)
        return fakeDb.toList()
    }

    override suspend fun fetchTopicsPaged(cursor: Int, limit: Int): PagedResult<TopicDto> {
        kotlinx.coroutines.delay(50)
        val page = fakeDb.drop(cursor).take(limit)
        val nextCursor = if (page.size == limit) cursor + limit else null
        return PagedResult(data = page, nextCursor = nextCursor, total = fakeDb.size)
    }

    override suspend fun createTopic(dto: TopicDto): TopicDto {
        val newId = (fakeDb.maxOfOrNull { it.id } ?: 0) + 1
        val created = dto.copy(id = newId)
        fakeDb.add(created)
        return created
    }

    override suspend fun updateTopic(id: Int, dto: TopicDto): TopicDto {
        val index = fakeDb.indexOfFirst { it.id == id }
        val updated = dto.copy(id = id)
        if (index >= 0) fakeDb[index] = updated
        return updated
    }

    override suspend fun deleteTopic(id: Int) {
        fakeDb.removeAll { it.id == id }
    }
}

