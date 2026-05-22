package com.notes.core.network.datasource

import com.notes.core.network.api.TopicsApi
import com.notes.core.network.dto.TopicDto
import com.notes.core.network.mapper.toTopicDto
import com.notes.core.network.pagination.PagedResult
import javax.inject.Inject

/**
 * Implementação REAL — chama https://jsonplaceholder.typicode.com/posts
 * e converte PostDto → TopicDto para o resto do app não saber da diferença.
 */
class TopicRemoteDataSourceImpl @Inject constructor(
    private val api: TopicsApi
) : TopicRemoteDataSource {

    override suspend fun fetchTopics(): List<TopicDto> =
        api.getPosts().map { it.toTopicDto() }

    /**
     * Cursor pagination usando _start/_limit do JSONPlaceholder.
     *
     * JSONPlaceholder tem 100 posts no total.
     * O "nextCursor" é calculado localmente: se recebemos [limit] itens,
     * provavelmente há mais. Em uma API real, o servidor informaria isso.
     */
    override suspend fun fetchTopicsPaged(cursor: Int, limit: Int): PagedResult<TopicDto> {
        val posts = api.getPostsPaged(start = cursor, limit = limit)
        val topics = posts.map { it.toTopicDto() }

        val nextCursor = if (topics.size == limit) cursor + limit else null

        return PagedResult(
            data = topics,
            nextCursor = nextCursor,
            total = 100 // JSONPlaceholder sempre tem 100 posts
        )
    }

    // JSONPlaceholder é read-only (POST/PUT/DELETE fazem fake no servidor)
    // mas deixamos implementado para quando migrar para API real
    override suspend fun createTopic(dto: TopicDto): TopicDto = dto
    override suspend fun updateTopic(id: Int, dto: TopicDto): TopicDto = dto
    override suspend fun deleteTopic(id: Int) = Unit
}
